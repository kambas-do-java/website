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