// Drag-and-drop moves and keyboard undo/redo for the formation board.
// Every move still goes through the server as a command, so the browser holds
// no state of its own; without JavaScript the "Move to" buttons do the same.
(function () {
  const moveForm = document.getElementById('move-form');
  if (!moveForm) return;

  document.querySelectorAll('.member[draggable="true"]').forEach(function (card) {
    card.addEventListener('dragstart', function (e) {
      e.dataTransfer.setData('text/plain', card.dataset.student);
      e.dataTransfer.effectAllowed = 'move';
      card.classList.add('dragging');
    });
    card.addEventListener('dragend', function () { card.classList.remove('dragging'); });
  });

  document.querySelectorAll('[data-dropzone]').forEach(function (zone) {
    const open = zone.dataset.open === 'true';
    zone.addEventListener('dragover', function (e) {
      if (!open) return;
      e.preventDefault();
      zone.classList.add('over');
    });
    zone.addEventListener('dragleave', function () { zone.classList.remove('over'); });
    zone.addEventListener('drop', function (e) {
      e.preventDefault();
      zone.classList.remove('over');
      const studentId = e.dataTransfer.getData('text/plain');
      if (!open || !studentId) return;
      moveForm.elements.studentId.value = studentId;
      moveForm.elements.toTeam.value = zone.dataset.dropzone;
      moveForm.submit();
    });
  });

  document.addEventListener('keydown', function (e) {
    if (!(e.ctrlKey || e.metaKey)) return;
    const tag = (e.target.tagName || '').toLowerCase();
    if (tag === 'input' || tag === 'select' || tag === 'textarea') return;
    const key = e.key.toLowerCase();
    const id = key === 'z' && !e.shiftKey ? 'undo-form'
             : (key === 'y' || (key === 'z' && e.shiftKey)) ? 'redo-form' : null;
    const form = id && document.getElementById(id);
    const button = form && form.querySelector('button');
    if (button && !button.disabled) { e.preventDefault(); form.submit(); }
  });
})();
