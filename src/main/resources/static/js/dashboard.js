/**
 * eLoan SaaS — dashboard.js
 * Vanilla JavaScript | No dependencies
 * Production-ready
 */

(function () {
  'use strict';

  /* ============================================================
     DOM References
     ============================================================ */
  const sidebar       = document.getElementById('appSidebar');
  const overlay       = document.getElementById('sidebarOverlay');
  const toggleBtn     = document.getElementById('sidebarToggle');
  const topbarSearch  = document.getElementById('topbarSearch');

  /* ============================================================
     Sidebar Toggle (Mobile)
     ============================================================ */
  function openSidebar() {
    if (!sidebar || !overlay) return;
    sidebar.classList.add('sidebar-open');
    overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
    if (toggleBtn) toggleBtn.setAttribute('aria-expanded', 'true');
  }

  function closeSidebar() {
    if (!sidebar || !overlay) return;
    sidebar.classList.remove('sidebar-open');
    overlay.classList.remove('active');
    document.body.style.overflow = '';
    if (toggleBtn) toggleBtn.setAttribute('aria-expanded', 'false');
  }

  function toggleSidebar() {
    if (!sidebar) return;
    sidebar.classList.contains('sidebar-open') ? closeSidebar() : openSidebar();
  }

  if (toggleBtn)  toggleBtn.addEventListener('click', toggleSidebar);
  if (overlay)    overlay.addEventListener('click', closeSidebar);

  /* Close sidebar on Escape */
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeSidebar();
  });

  /* Close sidebar when nav link clicked on mobile */
  if (sidebar) {
    sidebar.querySelectorAll('.nav-link').forEach(function (link) {
      link.addEventListener('click', function () {
        if (window.innerWidth <= 768) closeSidebar();
      });
    });
  }

  /* ============================================================
     Responsive: reset sidebar on resize
     ============================================================ */
  let resizeTimer;
  window.addEventListener('resize', function () {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(function () {
      if (window.innerWidth > 768) {
        closeSidebar();
        document.body.style.overflow = '';
      }
    }, 100);
  });

  /* ============================================================
     Active Nav Link — auto-detect from URL
     ============================================================ */
  (function setActiveNav() {
    if (!sidebar) return;
    const path = window.location.pathname;
    const links = sidebar.querySelectorAll('.nav-link');

    links.forEach(function (link) {
      link.classList.remove('active');
      const href = link.getAttribute('href');
      if (href && path === href) {
        link.classList.add('active');
      } else if (href && href !== '/' && path.startsWith(href)) {
        link.classList.add('active');
      }
    });
  })();

  /* ============================================================
     Search — keyboard shortcut (Ctrl/Cmd + K)
     ============================================================ */
  document.addEventListener('keydown', function (e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      if (topbarSearch) {
        topbarSearch.focus();
        topbarSearch.select();
      }
    }
  });

  /* ============================================================
     Animate stat cards on load
     ============================================================ */
  (function animateStatCards() {
    const cards = document.querySelectorAll('.stat-card');
    if (!cards.length) return;

    const observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry, i) {
        if (entry.isIntersecting) {
          entry.target.style.opacity = '0';
          entry.target.style.transform = 'translateY(16px)';
          setTimeout(function () {
            entry.target.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
          }, i * 80);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1 });

    cards.forEach(function (card) { observer.observe(card); });
  })();

  /* ============================================================
     Counter Animation for stat values
     ============================================================ */
  function animateCounter(el) {
    const raw = el.getAttribute('data-count');
    if (!raw) return;

    const isFloat   = raw.includes('.');
    const target    = parseFloat(raw.replace(/,/g, ''));
    const prefix    = el.getAttribute('data-prefix') || '';
    const suffix    = el.getAttribute('data-suffix') || '';
    const duration  = 1000;
    const start     = performance.now();

    function step(now) {
      const elapsed  = now - start;
      const progress = Math.min(elapsed / duration, 1);
      const ease     = 1 - Math.pow(1 - progress, 3);
      const current  = target * ease;

      let display;
      if (isFloat) {
        display = current.toFixed(1);
      } else {
        display = Math.floor(current).toLocaleString('en-IN');
      }

      el.textContent = prefix + display + suffix;

      if (progress < 1) requestAnimationFrame(step);
    }

    requestAnimationFrame(step);
  }

  (function initCounters() {
    const counters = document.querySelectorAll('[data-count]');
    if (!counters.length) return;

    const observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          animateCounter(entry.target);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });

    counters.forEach(function (el) { observer.observe(el); });
  })();

  /* ============================================================
     Tooltip: initialize Bootstrap tooltips
     ============================================================ */
  (function initTooltips() {
    if (typeof bootstrap === 'undefined') return;
    const tooltipEls = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipEls.forEach(function (el) {
      new bootstrap.Tooltip(el, { trigger: 'hover', placement: 'bottom' });
    });
  })();

  /* ============================================================
     Table Row Highlight
     ============================================================ */
  document.querySelectorAll('.data-table tbody tr').forEach(function (row) {
    row.style.cursor = 'pointer';
    row.addEventListener('click', function () {
      document.querySelectorAll('.data-table tbody tr').forEach(function (r) {
        r.style.background = '';
      });
      row.style.background = 'var(--primary-light)';
    });
  });

  /* ============================================================
     Toast Notification Utility
     Usage: window.showToast('Message', 'success' | 'danger' | 'warning')
     ============================================================ */
  window.showToast = function (message, type) {
    type = type || 'success';
    const icons = { success: 'bi-check-circle-fill', danger: 'bi-x-circle-fill', warning: 'bi-exclamation-circle-fill' };
    const colors = { success: 'var(--success)', danger: 'var(--danger)', warning: 'var(--warning)' };

    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:9999;display:flex;flex-direction:column;gap:8px;';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.cssText = [
      'display:flex;align-items:center;gap:10px;',
      'background:var(--bg-white);border:1px solid var(--border);',
      'border-radius:var(--radius-md);padding:12px 16px;',
      'box-shadow:var(--shadow-lg);font-size:0.875rem;font-family:var(--font-body);',
      'color:var(--text-primary);min-width:240px;max-width:320px;',
      'transform:translateX(100%);opacity:0;',
      'transition:transform 0.3s ease,opacity 0.3s ease;'
    ].join('');

    toast.innerHTML = '<i class="bi ' + (icons[type] || icons.success) + '" style="color:' + (colors[type] || colors.success) + ';font-size:1.1rem;flex-shrink:0;"></i>'
      + '<span style="flex:1;">' + message + '</span>'
      + '<button onclick="this.parentNode.remove()" style="border:none;background:none;color:var(--text-muted);cursor:pointer;padding:0;font-size:1rem;">'
      + '<i class="bi bi-x"></i></button>';

    container.appendChild(toast);

    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        toast.style.transform = 'translateX(0)';
        toast.style.opacity = '1';
      });
    });

    setTimeout(function () {
      toast.style.transform = 'translateX(100%)';
      toast.style.opacity = '0';
      setTimeout(function () { toast.remove(); }, 300);
    }, 4000);
  };

  /* ============================================================
     Init complete
     ============================================================ */
  console.info('[eLoan SaaS] Dashboard JS initialized ✓');

})();