// Funcionalidades para a página All Posts
document.addEventListener('DOMContentLoaded', function() {
    // Busca de artigos
    const searchInput = document.getElementById('post-search');
    const searchBtn = document.querySelector('.search-btn');

    if (searchInput && searchBtn) {
        searchBtn.addEventListener('click', performSearch);
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }

    // Filtros por tags
    const filterTags = document.querySelectorAll('.filter-tag');
    filterTags.forEach(tag => {
        tag.addEventListener('click', function() {
            // Remove active class de todos os botões
            filterTags.forEach(t => t.classList.remove('active'));
            // Adiciona active class ao botão clicado
            this.classList.add('active');

            const filter = this.getAttribute('data-filter');
            filterPosts(filter);
        });
    });

    function performSearch() {
        const searchTerm = searchInput.value.toLowerCase().trim();
        if (searchTerm) {
            filterPosts('search', searchTerm);
        } else {
            filterPosts('all');
        }
    }

    function filterPosts(filter, term = '') {
        const postItems = document.querySelectorAll('.post-item');

        postItems.forEach(item => {
            let showItem = false;

            if (filter === 'all') {
                showItem = true;
            } else if (filter === 'search') {
                const title = item.querySelector('h4 a').textContent.toLowerCase();
                const excerpt = item.querySelector('.post-excerpt').textContent.toLowerCase();
                showItem = title.includes(term) || excerpt.includes(term);
            } else {
                const category = item.querySelector('.post-category').textContent.toLowerCase();
                showItem = category.includes(filter);
            }

            item.style.display = showItem ? 'flex' : 'none';

            // Esconde seções vazias
            const monthSection = item.closest('.month-section');
            if (monthSection) {
                const visiblePosts = monthSection.querySelectorAll('.post-item[style*="display: flex"]');
                monthSection.style.display = visiblePosts.length > 0 ? 'block' : 'none';
            }

            const yearSection = item.closest('.year-section');
            if (yearSection) {
                const visibleMonths = yearSection.querySelectorAll('.month-section[style*="display: block"]');
                yearSection.style.display = visibleMonths.length > 0 ? 'block' : 'none';
            }
        });

        // Mostra mensagem se não houver resultados
        const visiblePosts = document.querySelectorAll('.post-item[style*="display: flex"]');
        const emptyState = document.querySelector('.empty-state') || createEmptyState();

        if (visiblePosts.length === 0) {
            emptyState.style.display = 'block';
        } else {
            emptyState.style.display = 'none';
        }
    }

    function createEmptyState() {
        const emptyState = document.createElement('div');
        emptyState.className = 'empty-state';
        emptyState.innerHTML = `
            <h3>Nenhum artigo encontrado</h3>
            <p>Tente ajustar sua busca ou filtro para encontrar o que procura.</p>
        `;
        document.querySelector('.all-posts-content').appendChild(emptyState);
        return emptyState;
    }
});

// Funcionalidades para a página All Members
document.addEventListener('DOMContentLoaded', function() {
    // Busca de membros
    const memberSearchInput = document.getElementById('member-search');

    if (memberSearchInput) {
        memberSearchInput.addEventListener('input', performMemberSearch);
    }

    function performMemberSearch() {
        const searchTerm = memberSearchInput.value.toLowerCase().trim();
        filterMembers(searchTerm);
    }

    function filterMembers(term = '') {
        const memberCards = document.querySelectorAll('.member-card');

        memberCards.forEach(card => {
            const memberName = card.querySelector('h3').textContent.toLowerCase();
            const showItem = memberName.includes(term);
            card.style.display = showItem ? 'block' : 'none';
        });

        // Mostra mensagem se não houver resultados
        const visibleMembers = document.querySelectorAll('.member-card[style*="display: block"]');
        const emptyState = document.querySelector('.empty-state-members') || createEmptyStateMembers();

        if (visibleMembers.length === 0) {
            emptyState.style.display = 'block';
        } else {
            emptyState.style.display = 'none';
        }
    }

    function createEmptyStateMembers() {
        const emptyState = document.createElement('div');
        emptyState.className = 'empty-state-members';
        emptyState.innerHTML = `
            <h3>Nenhum membro encontrado</h3>
            <p>Tente ajustar sua busca para encontrar o que procura.</p>
        `;
        document.querySelector('.members-grid').appendChild(emptyState);
        return emptyState;
    }
});

// ===================================
// MENU HAMBURGER
// ===================================

document.addEventListener('DOMContentLoaded', function() {
  // Menu Toggle
  const menuToggle = document.querySelector('.menu-toggle');
  const nav = document.querySelector('nav');
  
  if (menuToggle) {
    menuToggle.addEventListener('click', function() {
      this.classList.toggle('active');
      nav.classList.toggle('active');
    });
    
    // Fechar menu ao clicar em um link
    const navLinks = nav.querySelectorAll('a');
    navLinks.forEach(link => {
      link.addEventListener('click', () => {
        menuToggle.classList.remove('active');
        nav.classList.remove('active');
      });
    });
    
    // Fechar menu ao clicar fora
    document.addEventListener('click', function(e) {
      if (!menuToggle.contains(e.target) && !nav.contains(e.target)) {
        menuToggle.classList.remove('active');
        nav.classList.remove('active');
      }
    });
  }
  
  // ===================================
  // SCROLL TO TOP BUTTON
  // ===================================
  
  const scrollToTop = document.createElement('button');
  scrollToTop.className = 'scroll-to-top';
  scrollToTop.innerHTML = '↑';
  scrollToTop.setAttribute('aria-label', 'Voltar ao topo');
  document.body.appendChild(scrollToTop);
  
  window.addEventListener('scroll', function() {
    if (window.pageYOffset > 300) {
      scrollToTop.classList.add('visible');
    } else {
      scrollToTop.classList.remove('visible');
    }
  });
  
  scrollToTop.addEventListener('click', function() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  });
  
  // ===================================
  // READING PROGRESS BAR
  // ===================================
  
  if (document.querySelector('.article-content, .member-content, .event-content')) {
    const progressBar = document.createElement('div');
    progressBar.className = 'progress-bar';
    document.body.prepend(progressBar);
    
    window.addEventListener('scroll', function() {
      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight - windowHeight;
      const scrollTop = window.pageYOffset;
      const scrollPercent = (scrollTop / documentHeight) * 100;
      
      progressBar.style.width = scrollPercent + '%';
    });
  }
  
  // ===================================
  // CODE COPY BUTTON
  // ===================================
  
  const codeBlocks = document.querySelectorAll('pre code');
  
  codeBlocks.forEach((codeBlock, index) => {
    const pre = codeBlock.parentElement;
    
    // Criar wrapper se não existir
    if (!pre.parentElement.classList.contains('code-block')) {
      const wrapper = document.createElement('div');
      wrapper.className = 'code-block';
      pre.parentElement.insertBefore(wrapper, pre);
      wrapper.appendChild(pre);
      
      // Detectar linguagem
      const language = detectLanguage(codeBlock);
      
      // Criar header
      const header = document.createElement('div');
      header.className = 'code-header';
      header.innerHTML = `
        <span class="code-language">${language}</span>
        <div class="code-actions">
          <button class="code-copy" data-code-index="${index}">Copiar</button>
        </div>
      `;
      
      wrapper.insertBefore(header, pre);
    }
  });
  
  // Função para detectar linguagem
  function detectLanguage(codeBlock) {
    const classList = codeBlock.classList;
    for (let className of classList) {
      if (className.startsWith('language-')) {
        return className.replace('language-', '').toUpperCase();
      }
    }
    
    // Detectar por conteúdo
    const code = codeBlock.textContent;
    if (code.includes('public class') || code.includes('import java')) return 'JAVA';
    if (code.includes('function') || code.includes('const ') || code.includes('let ')) return 'JAVASCRIPT';
    if (code.includes('def ') || code.includes('import ')) return 'PYTHON';
    if (code.includes('<html>') || code.includes('<!DOCTYPE')) return 'HTML';
    if (code.includes('{') && code.includes('color:')) return 'CSS';
    
    return 'CODE';
  }
  
  // Event listener para botões de copiar
  document.addEventListener('click', function(e) {
    if (e.target.classList.contains('code-copy')) {
      const index = e.target.getAttribute('data-code-index');
      const codeBlock = codeBlocks[index];
      const code = codeBlock.textContent;
      
      // Copiar para clipboard
      navigator.clipboard.writeText(code).then(() => {
        const button = e.target;
        const originalText = button.textContent;
        
        button.textContent = 'Copiado!';
        button.classList.add('copied');
        
        setTimeout(() => {
          button.textContent = originalText;
          button.classList.remove('copied');
        }, 2000);
      }).catch(err => {
        console.error('Erro ao copiar:', err);
        alert('Não foi possível copiar o código');
      });
    }
  });
  
  // ===================================
  // BUSCA DE POSTS/MEMBROS
  // ===================================
  
  const searchInput = document.getElementById('post-search') || document.getElementById('member-search');
  
  if (searchInput) {
    searchInput.addEventListener('input', function(e) {
      const searchTerm = e.target.value.toLowerCase();
      const items = document.querySelectorAll('.post-item, .member-card');
      
      items.forEach(item => {
        const text = item.textContent.toLowerCase();
        if (text.includes(searchTerm)) {
          item.style.display = '';
          item.classList.add('animate-fade-in');
        } else {
          item.style.display = 'none';
        }
      });
      
      // Verificar se há resultados
      const visibleItems = Array.from(items).filter(item => item.style.display !== 'none');
      const container = items[0]?.parentElement;
      
      if (container && visibleItems.length === 0) {
        if (!document.querySelector('.no-results')) {
          const noResults = document.createElement('div');
          noResults.className = 'empty-state no-results';
          noResults.innerHTML = `
            <h3>Nenhum resultado encontrado</h3>
            <p>Tente usar palavras-chave diferentes.</p>
          `;
          container.appendChild(noResults);
        }
      } else {
        const noResults = document.querySelector('.no-results');
        if (noResults) noResults.remove();
      }
    });
  }
  
  // ===================================
  // FILTROS DE TAGS
  // ===================================
  
  const filterTags = document.querySelectorAll('.filter-tag');
  
  filterTags.forEach(tag => {
    tag.addEventListener('click', function() {
      // Remover active de todos
      filterTags.forEach(t => t.classList.remove('active'));
      
      // Adicionar active ao clicado
      this.classList.add('active');
      
      const filter = this.getAttribute('data-filter');
      const items = document.querySelectorAll('.post-item');
      
      items.forEach(item => {
        if (filter === 'all') {
          item.style.display = '';
        } else {
          const categories = item.getAttribute('data-categories') || '';
          if (categories.includes(filter)) {
            item.style.display = '';
          } else {
            item.style.display = 'none';
          }
        }
      });
    });
  });
  
  // ===================================
  // SMOOTH SCROLL PARA ÂNCORAS
  // ===================================
  
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
      const href = this.getAttribute('href');
      
      // Ignorar # sozinho
      if (href === '#') return;
      
      e.preventDefault();
      
      const target = document.querySelector(href);
      if (target) {
        const headerOffset = 80;
        const elementPosition = target.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
        
        window.scrollTo({
          top: offsetPosition,
          behavior: 'smooth'
        });
      }
    });
  });
  
  // ===================================
  // LAZY LOADING DE IMAGENS
  // ===================================
  
  if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target;
          img.src = img.dataset.src || img.src;
          img.classList.add('loaded');
          observer.unobserve(img);
        }
      });
    });
    
    document.querySelectorAll('img[data-src]').forEach(img => {
      imageObserver.observe(img);
    });
  }
  
  // ===================================
  // ANIMAÇÃO AO SCROLL
  // ===================================
  
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-fade-in');
      }
    });
  }, observerOptions);
  
  document.querySelectorAll('.feature, .post-preview, .event, .member-card').forEach(el => {
    observer.observe(el);
  });
  
  // ===================================
  // FORM VALIDATION
  // ===================================
  
  const contactForm = document.querySelector('.contact-form form');
  
  if (contactForm) {
    contactForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const formData = new FormData(this);
      const data = Object.fromEntries(formData);
      
      // Validação básica
      let isValid = true;
      
      if (!data.email || !data.email.includes('@')) {
        alert('Por favor, insira um email válido.');
        isValid = false;
      }
      
      if (isValid) {
        // Aqui você pode adicionar a lógica de envio
        alert('Mensagem enviada com sucesso! (Demo)');
        this.reset();
      }
    });
  }
  
  // ===================================
  // NEWSLETTER FORM
  // ===================================
  
  const newsletterForm = document.querySelector('.newsletter-form');
  
  if (newsletterForm) {
    newsletterForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const email = this.querySelector('input[type="email"]').value;
      
      if (email && email.includes('@')) {
        alert('Obrigado por se inscrever! (Demo)');
        this.reset();
      } else {
        alert('Por favor, insira um email válido.');
      }
    });
  }
  
  // ===================================
  // STICKY HEADER
  // ===================================
  
  const header = document.querySelector('header');
  let lastScroll = 0;
  
  window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;
    
    if (currentScroll <= 0) {
      header.classList.remove('scroll-up');
      return;
    }
    
    if (currentScroll > lastScroll && !header.classList.contains('scroll-down')) {
      // Scroll para baixo
      header.classList.remove('scroll-up');
      header.classList.add('scroll-down');
    } else if (currentScroll < lastScroll && header.classList.contains('scroll-down')) {
      // Scroll para cima
      header.classList.remove('scroll-down');
      header.classList.add('scroll-up');
    }
    
    lastScroll = currentScroll;
  });
  
  // ===================================
  // DARK MODE TOGGLE (Opcional)
  // ===================================
  
  const darkModeToggle = document.querySelector('.dark-mode-toggle');
  
  if (darkModeToggle) {
    // Verificar preferência salva
    const darkMode = localStorage.getItem('darkMode');
    
    if (darkMode === 'enabled') {
      document.body.classList.add('dark-mode');
    }
    
    darkModeToggle.addEventListener('click', () => {
      document.body.classList.toggle('dark-mode');
      
      if (document.body.classList.contains('dark-mode')) {
        localStorage.setItem('darkMode', 'enabled');
      } else {
        localStorage.setItem('darkMode', null);
      }
    });
  }
  
  // ===================================
  // TOOLTIP ACTIVATION
  // ===================================
  
  const tooltips = document.querySelectorAll('[data-tooltip]');
  
  tooltips.forEach(tooltip => {
    tooltip.addEventListener('mouseenter', function() {
      this.setAttribute('aria-label', this.getAttribute('data-tooltip'));
    });
  });
  
  // ===================================
  // KEYBOARD NAVIGATION
  // ===================================
  
  document.addEventListener('keydown', function(e) {
    // ESC fecha modais e menus
    if (e.key === 'Escape') {
      const modal = document.querySelector('.modal.active');
      if (modal) {
        modal.classList.remove('active');
      }
      
      if (menuToggle && menuToggle.classList.contains('active')) {
        menuToggle.classList.remove('active');
        nav.classList.remove('active');
      }
    }
  });
  
  // ===================================
  // SYNTAX HIGHLIGHTING BÁSICO
  // ===================================
  
  function highlightCode() {
    const codeBlocks = document.querySelectorAll('pre code:not(.highlighted)');
    
    codeBlocks.forEach(block => {
      let code = block.innerHTML;
      
      // Java/JavaScript keywords
      const keywords = ['public', 'private', 'class', 'function', 'const', 'let', 'var', 
                       'if', 'else', 'for', 'while', 'return', 'import', 'package',
                       'static', 'void', 'int', 'String', 'boolean', 'new', 'this'];
      
      keywords.forEach(keyword => {
        const regex = new RegExp(`\\b(${keyword})\\b`, 'g');
        code = code.replace(regex, '<span class="keyword">$1</span>');
      });
      
      // Strings
      code = code.replace(/(".*?"|'.*?')/g, '<span class="string">$1</span>');
      
      // Comments
      code = code.replace(/(\/\/.*$)/gm, '<span class="comment">$1</span>');
      code = code.replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="comment">$1</span>');
      
      // Numbers
      code = code.replace(/\b(\d+)\b/g, '<span class="number">$1</span>');
      
      block.innerHTML = code;
      block.classList.add('highlighted');
    });
  }
  
  highlightCode();
  
  // ===================================
  // PERFORMANCE: DEBOUNCE
  // ===================================
  
  function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }
  
  // Aplicar debounce em eventos de scroll e resize
  const debouncedScroll = debounce(() => {
    // Código que precisa executar no scroll
  }, 100);
  
  window.addEventListener('scroll', debouncedScroll);
  
  // ===================================
  // ANALYTICS (placeholder)
  // ===================================
  
  function trackEvent(category, action, label) {
    // Adicione seu código de analytics aqui
    console.log('Event tracked:', category, action, label);
  }
  
  // Track clicks em links externos
  document.querySelectorAll('a[target="_blank"]').forEach(link => {
    link.addEventListener('click', () => {
      trackEvent('External Link', 'Click', link.href);
    });
  });
  
  console.log('🚀 Kambas do Java - Site carregado com sucesso!');
});