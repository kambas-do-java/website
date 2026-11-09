const btn = document.querySelector('.menu-btn');
const nav = document.getElementById('nav');
btn?.addEventListener('click', () => {
  const open = nav.classList.toggle('open');
  btn.setAttribute('aria-expanded', open);
});

// Filtro dos artigos: pesquisa de texto + tags (todas as tags escolhidas têm de coincidir).
const bar = document.querySelector('.filter');
if (bar) {
  bar.hidden = false;
  const q = bar.querySelector('input');
  const chips = [...bar.querySelectorAll('.chip')];
  const rows = [...document.querySelectorAll('.routes .route')];
  const groups = [...document.querySelectorAll('.routes .group')];
  const count = bar.querySelector('.count');
  const none = document.querySelector('.none');
  const norm = s => s.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');

  const apply = () => {
    const text = norm(q.value.trim());
    const tags = chips.filter(c => c.getAttribute('aria-pressed') === 'true').map(c => c.dataset.tag);
    let shown = 0;
    rows.forEach(r => {
      const ok = (!text || norm(r.dataset.q).includes(text)) &&
        tags.every(t => r.dataset.tags.split('|').includes(t));
      r.hidden = !ok;
      if (ok) shown++;
    });
    groups.forEach(g => {
      let n = g.nextElementSibling, any = false;
      while (n && !n.classList.contains('group')) { if (!n.hidden) any = true; n = n.nextElementSibling; }
      g.hidden = !any;
    });
    count.textContent = shown === rows.length ? '' : `${shown} de ${rows.length} artigos`;
    none.hidden = shown > 0;
  };

  q.addEventListener('input', apply);
  chips.forEach(c => c.addEventListener('click', () => {
    c.setAttribute('aria-pressed', c.getAttribute('aria-pressed') !== 'true');
    apply();
  }));
  const inicial = new URLSearchParams(location.search).get('tag');
  chips.filter(c => c.dataset.tag === (inicial || '').toLowerCase())
    .forEach(c => c.setAttribute('aria-pressed', 'true'));
  apply();
}
