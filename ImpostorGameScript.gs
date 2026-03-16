/**
 * ImpostorGameScript.gs
 * Google Apps Script — ejecutar con la cuenta modinoricardo@gmail.com
 *
 * SETUP (una sola vez, en este orden):
 *   1. Ve a script.google.com e inicia sesión con modinoricardo@gmail.com
 *   2. Crea un proyecto nuevo y pega TODO este archivo
 *   3. Ejecuta  setProperties()  → acepta los permisos que pida Google
 *   4. Ejecuta  setupTrigger()
 *   Listo. Cada 5 minutos el script comprobará si respondiste a algún email
 *   de sugerencia y, si es así, creará la tarea en Notion automáticamente.
 */

// ── Configuración ─────────────────────────────────────────────────────────────

function setProperties() {
  PropertiesService.getScriptProperties().setProperties({
    'NOTION_TOKEN'   : '***NOTION_TOKEN_REMOVED***',
    'NOTION_PAGE_ID' : '32460070-d18b-802b-8428-d7f24f2d98c9'
  });
  Logger.log('Propiedades guardadas correctamente.');
}

function setupTrigger() {
  ScriptApp.getProjectTriggers().forEach(t => ScriptApp.deleteTrigger(t));
  ScriptApp.newTrigger('checkReplies')
    .timeBased()
    .everyMinutes(5)
    .create();
  Logger.log('Trigger creado: checkReplies cada 5 minutos.');
}

// ── Lógica principal ──────────────────────────────────────────────────────────

function checkReplies() {
  const label   = getOrCreateLabel('ImpostorGame/Procesado');
  const threads = GmailApp.search('subject:[ImpostorGame] -label:ImpostorGame/Procesado');

  threads.forEach(thread => {
    const messages = thread.getMessages();
    if (messages.length < 2) return;

    const asunto = thread.getFirstMessageSubject();
    const titulo = asunto.replace(/\[ImpostorGame\]\s*/i, '').trim();
    const cuerpo = messages[0].getPlainBody();
    const datos  = parsearCuerpo(cuerpo);

    crearTareaNotion(titulo, datos);
    thread.addLabel(label);
    Logger.log('Tarea creada en Notion: "' + titulo + '"');
  });
}

// ── Parseo del email ──────────────────────────────────────────────────────────

function parsearCuerpo(cuerpo) {
  // Normalizar saltos de línea (los clientes de email usan \r\n)
  const texto = cuerpo.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

  function extraer(desde, hasta) {
    const ini = texto.indexOf(desde);
    if (ini === -1) return '';
    const start = ini + desde.length;
    const end   = hasta ? texto.indexOf(hasta, start) : texto.length;
    return texto.substring(start, end !== -1 ? end : texto.length).trim();
  }

  return {
    usuario     : extraer('USUARIO   : ', '\n'),
    dispositivo : extraer('DISPOSITIVO: ', '\n'),
    mensaje     : extraer('--- MENSAJE USUARIO ---\n', '\n' + '='.repeat(40))
  };
}

// ── Notion API ────────────────────────────────────────────────────────────────

function crearTareaNotion(titulo, datos) {
  const props  = PropertiesService.getScriptProperties();
  const token  = props.getProperty('NOTION_TOKEN');
  const pageId = props.getProperty('NOTION_PAGE_ID');
  const hdrs   = {
    'Authorization' : 'Bearer ' + token,
    'Content-Type'  : 'application/json',
    'Notion-Version': '2022-06-28'
  };

  const fecha = Utilities.formatDate(new Date(), Session.getScriptTimeZone(), 'dd/MM/yyyy HH:mm');

  // Paso 1: Obtener (o crear en el primer uso) la página archivo donde
  //         guardaremos las sub-páginas de detalles. Al crearlas aquí
  //         no aparecen directamente en la página principal → sin duplicados.
  const archiveId = getOrCreateArchivePage(pageId, hdrs);

  // Paso 2: Crear sub-página de detalles dentro de la página archivo
  const childResp = notionFetch('https://api.notion.com/v1/pages', 'post', {
    parent    : { page_id: archiveId },
    properties: { title: { title: [{ type: 'text', text: { content: titulo } }] } },
    children  : [
      heading2('Datos del usuario'),
      paragraph('📧  ' + (datos.usuario     || 'No proporcionado')),
      paragraph('📱  ' + (datos.dispositivo || 'Desconocido')),
      paragraph('📅  ' + fecha)
    ]
  }, hdrs);
  const childId = JSON.parse(childResp.getContentText()).id;

  // Paso 3: Obtener (o crear en el primer uso) el bloque centinela.
  //         Insertando siempre "after: centinela", las nuevas tareas
  //         aparecen en la parte superior de la lista.
  const sentinelId = getOrCreateSentinel(pageId, hdrs);

  // Paso 4: Añadir checkbox con enlace a la sub-página, justo después del centinela
  notionFetch(
    'https://api.notion.com/v1/blocks/' + pageId + '/children',
    'patch',
    {
      after   : sentinelId,
      children: [{
        type  : 'to_do',
        to_do : {
          checked  : false,
          rich_text: [{
            type   : 'mention',
            mention: { type: 'page', page: { id: childId } }
          }]
        }
      }]
    },
    hdrs
  );
}

// ── Inicialización automática (primera ejecución) ─────────────────────────────

/** Crea una sub-página "📁 Detalles sugerencias" y guarda su ID.
 *  Las siguientes llamadas devuelven el ID guardado sin tocar Notion. */
function getOrCreateArchivePage(pageId, hdrs) {
  const props = PropertiesService.getScriptProperties();
  let archiveId = props.getProperty('NOTION_ARCHIVE_PAGE_ID');
  if (archiveId) return archiveId;

  const resp = notionFetch('https://api.notion.com/v1/pages', 'post', {
    parent    : { page_id: pageId },
    icon      : { type: 'emoji', emoji: '📁' },
    properties: { title: { title: [{ type: 'text', text: { content: '📁 Detalles sugerencias' } }] } }
  }, hdrs);
  archiveId = JSON.parse(resp.getContentText()).id;
  props.setProperty('NOTION_ARCHIVE_PAGE_ID', archiveId);
  Logger.log('Página archivo creada: ' + archiveId);
  return archiveId;
}

/** Crea un separador (divider) que actúa de centinela para insertar
 *  las nuevas tareas siempre en la parte superior. */
function getOrCreateSentinel(pageId, hdrs) {
  const props = PropertiesService.getScriptProperties();
  let sentinelId = props.getProperty('NOTION_SENTINEL_ID');
  if (sentinelId) return sentinelId;

  const resp = notionFetch(
    'https://api.notion.com/v1/blocks/' + pageId + '/children',
    'patch',
    { children: [{ type: 'divider', divider: {} }] },
    hdrs
  );
  sentinelId = JSON.parse(resp.getContentText()).results[0].id;
  props.setProperty('NOTION_SENTINEL_ID', sentinelId);
  Logger.log('Centinela creado: ' + sentinelId);
  return sentinelId;
}

// ── Helpers de red ────────────────────────────────────────────────────────────

function notionFetch(url, method, payload, headers) {
  const options = {
    method            : method,
    headers           : headers,
    muteHttpExceptions: true
  };
  if (payload) options.payload = JSON.stringify(payload);
  return UrlFetchApp.fetch(url, options);
}

// ── Helpers de bloques Notion ─────────────────────────────────────────────────

function paragraph(text) {
  return { type: 'paragraph', paragraph: { rich_text: [{ type: 'text', text: { content: text } }] } };
}

function heading2(text) {
  return { type: 'heading_2', heading_2: { rich_text: [{ type: 'text', text: { content: text } }] } };
}

function getOrCreateLabel(name) {
  return GmailApp.getUserLabelByName(name) || GmailApp.createLabel(name);
}
