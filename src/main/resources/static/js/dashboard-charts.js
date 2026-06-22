/* =============================================
   dashboard-charts.js
   Place at: src/main/resources/static/js/dashboard-charts.js
============================================= */

document.addEventListener('DOMContentLoaded', () => {

    // ── COLORS ──────────────────────────────────
    const COLORS = {
        green:       '#2C5F2E',
        greenLight:  'rgba(44,95,46,.12)',
        greenMid:    'rgba(44,95,46,.7)',
        gold:        '#B7770D',
        danger:      '#C0392B',
        blue:        '#2461C0',
        purple:      '#7B2D8B',
        teal:        '#0E7C86',
        orange:      '#C86B19',
        ink3:        '#8C8880',
        border:      '#E8E5DC',
    };

    const PALETTE = [
        COLORS.green,
        COLORS.blue,
        COLORS.gold,
        COLORS.danger,
        COLORS.purple,
        COLORS.teal,
        COLORS.orange,
    ];

    // Chart.js default font
    Chart.defaults.font.family = "'Plus Jakarta Sans', sans-serif";
    Chart.defaults.color       = '#8C8880';

    // ── THIS MONTH vs LAST MONTH BADGE ──────────
    const badge    = document.getElementById('month-trend-badge');
    const thisM    = parseFloat(THIS_MONTH) || 0;
    const lastM    = parseFloat(LAST_MONTH) || 0;

    if (badge) {
        if (thisM === 0 && lastM === 0) {
            badge.textContent = '— No data';
            badge.classList.add('trend-same');
        } else if (lastM === 0) {
            badge.textContent = '▲ New';
            badge.classList.add('trend-up');
        } else {
            const pct = ((thisM - lastM) / lastM * 100).toFixed(1);
            if (pct > 0) {
                badge.textContent = `▲ ${pct}% vs last month`;
                badge.classList.add('trend-up');
            } else if (pct < 0) {
                badge.textContent = `▼ ${Math.abs(pct)}% vs last month`;
                badge.classList.add('trend-down');
            } else {
                badge.textContent = '→ Same as last month';
                badge.classList.add('trend-same');
            }
        }
    }

    // ── 1. MONTHLY TREND — Line Chart ───────────
    const trendCtx = document.getElementById('trendChart');
    if (trendCtx && TREND_LABELS.length > 0) {
        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: TREND_LABELS,
                datasets: [{
                    label: 'Purchase (৳)',
                    data: TREND_VALUES,
                    borderColor: COLORS.green,
                    backgroundColor: COLORS.greenLight,
                    borderWidth: 2.5,
                    pointBackgroundColor: COLORS.green,
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 7,
                    fill: true,
                    tension: 0.4,
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1A1814',
                        titleColor: '#fff',
                        bodyColor: '#D4CFC2',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: ctx => ' ৳' + Number(ctx.raw).toLocaleString('en-US', { minimumFractionDigits: 2 })
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { color: '#F0EDE6' },
                        ticks: { font: { size: 11 } }
                    },
                    y: {
                        grid: { color: '#F0EDE6' },
                        ticks: {
                            font: { size: 11 },
                            callback: val => '৳' + Number(val).toLocaleString()
                        }
                    }
                }
            }
        });
    } else if (trendCtx) {
        showEmptyChart(trendCtx, 'No purchase data yet');
    }

    // ── 2. PAYMENT METHOD — Doughnut ────────────
    const donutCtx = document.getElementById('donutChart');
    if (donutCtx && PAY_LABELS.length > 0) {
        new Chart(donutCtx, {
            type: 'doughnut',
            data: {
                labels: PAY_LABELS,
                datasets: [{
                    data: PAY_VALUES,
                    backgroundColor: PALETTE.slice(0, PAY_LABELS.length),
                    borderColor: '#fff',
                    borderWidth: 3,
                    hoverOffset: 6,
                }]
            },
            options: {
                responsive: true,
                cutout: '68%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1A1814',
                        titleColor: '#fff',
                        bodyColor: '#D4CFC2',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: ctx => ' ৳' + Number(ctx.raw).toLocaleString('en-US', { minimumFractionDigits: 2 })
                        }
                    }
                }
            }
        });

        // Custom legend
        const legend = document.getElementById('donut-legend');
        if (legend) {
            PAY_LABELS.forEach((label, i) => {
                const item = document.createElement('div');
                item.className = 'legend-item';
                item.innerHTML = `
                    <span class="legend-dot" style="background:${PALETTE[i]}"></span>
                    <span>${label}</span>
                `;
                legend.appendChild(item);
            });
        }
    } else if (donutCtx) {
        showEmptyChart(donutCtx, 'No payment data yet');
    }

    // ── 3. TOP SUPPLIERS — Horizontal Bar ───────
    const supCtx = document.getElementById('supplierChart');
    if (supCtx && SUP_LABELS.length > 0) {
        new Chart(supCtx, {
            type: 'bar',
            data: {
                labels: SUP_LABELS,
                datasets: [{
                    label: 'Purchase Total (৳)',
                    data: SUP_VALUES,
                    backgroundColor: PALETTE.slice(0, SUP_LABELS.length),
                    borderRadius: 6,
                    borderSkipped: false,
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1A1814',
                        titleColor: '#fff',
                        bodyColor: '#D4CFC2',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: ctx => ' ৳' + Number(ctx.raw).toLocaleString('en-US', { minimumFractionDigits: 2 })
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { color: '#F0EDE6' },
                        ticks: {
                            font: { size: 10 },
                            callback: val => '৳' + Number(val).toLocaleString()
                        }
                    },
                    y: {
                        grid: { display: false },
                        ticks: { font: { size: 11 } }
                    }
                }
            }
        });
    } else if (supCtx) {
        showEmptyChart(supCtx, 'No supplier data yet');
    }

    // ── 4. TOP PRODUCTS — Vertical Bar ──────────
    const prodCtx = document.getElementById('productChart');
    if (prodCtx && PROD_LABELS.length > 0) {
        new Chart(prodCtx, {
            type: 'bar',
            data: {
                labels: PROD_LABELS,
                datasets: [{
                    label: 'Qty Purchased',
                    data: PROD_VALUES,
                    backgroundColor: COLORS.greenMid,
                    hoverBackgroundColor: COLORS.green,
                    borderRadius: 6,
                    borderSkipped: false,
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1A1814',
                        titleColor: '#fff',
                        bodyColor: '#D4CFC2',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: ctx => ' Qty: ' + Number(ctx.raw).toLocaleString()
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: {
                            font: { size: 10 },
                            maxRotation: 30,
                        }
                    },
                    y: {
                        grid: { color: '#F0EDE6' },
                        ticks: { font: { size: 10 } }
                    }
                }
            }
        });
    } else if (prodCtx) {
        showEmptyChart(prodCtx, 'No product data yet');
    }

    // ── EMPTY CHART HELPER ───────────────────────
    function showEmptyChart(canvas, message) {
        const parent = canvas.parentElement;
        canvas.style.display = 'none';
        const div = document.createElement('div');
        div.style.cssText = `
            padding: 40px 20px;
            text-align: center;
            color: #8C8880;
            font-size: 13px;
            font-family: 'Plus Jakarta Sans', sans-serif;
        `;
        div.textContent = message;
        parent.appendChild(div);
    }

});
