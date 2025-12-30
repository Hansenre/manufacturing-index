/* =====================================================
   REGISTROS
   ===================================================== */
Chart.register(ChartDataLabels);
Chart.register(window['chartjs-plugin-annotation']);

let chart;

/* =====================================================
   LOAD DASHBOARD
   ===================================================== */
async function loadDashboard() {

    const fy = document.getElementById("fySelect")?.value;
    const quarter = document.getElementById("quarterSelect")?.value;

    if (!fy || !quarter || typeof factoryId === "undefined") {
        console.warn("Contexto incompleto:", { factoryId, fy, quarter });
        return;
    }

    const url = `/hfpi/dashboard/${factoryId}?fy=${fy}&quarter=${quarter}`;

    const res = await fetch(url);
    const data = await res.json();

    renderChart(data);
}

/* =====================================================
   RENDER CHART
   ===================================================== */
function renderChart(data) {

    const ctx = document.getElementById("hfpiChart");

    if (!ctx) {
        console.error("Canvas hfpiChart não encontrado");
        return;
    }

    if (chart) {
        chart.destroy();
        chart = null;
    }

    // API retorna valores em %
	const online  = Number(data.hfpiOnline ?? 0) * 100;
	const factory = Number(data.hfpiFactory ?? 0) * 100;
	const finalV  = Number(data.hfpiFinal ?? 0) * 100;

    chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['HFPI FINAL', 'HFPI ONLINE', 'HFPI FACTORY'],
            datasets: [{
                data: [finalV, online, factory],
                backgroundColor: ['#1976d2', '#ef6c00', '#2e7d32']
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    min: 0,
                    max: 100,
                    ticks: {
                        callback: value => value + '%'
                    }
                }
            },
            plugins: {
                legend: { display: false },

                tooltip: {
                    callbacks: {
                        label: ctx => ctx.raw.toFixed(2) + '%'
                    }
                },

                datalabels: {
                    anchor: 'end',
                    align: 'right',
                    formatter: value => value.toFixed(1) + '%',
                    color: '#000',
                    font: {
                        weight: 'bold',
                        size: 12
                    }
                },

                annotation: {
                    annotations: {
                        bronze: {
                            type: 'line',
                            scaleID: 'x',
                            value: 94,
                            borderColor: '#cd7f32',
                            borderWidth: 2,
                            label: {
                                display: true,
                                content: 'Bronze 94%',
                                position: 'end',
                                backgroundColor: '#cd7f32',
                                color: '#fff'
                            }
                        },
                        prata: {
                            type: 'line',
                            scaleID: 'x',
                            value: 97,
                            borderColor: '#c0c0c0',
                            borderWidth: 2,
                            label: {
                                display: true,
                                content: 'Prata 97%',
                                position: 'end',
                                backgroundColor: '#c0c0c0',
                                color: '#000'
                            }
                        }
                    }
                }
            }
        }
    });
}

/* =====================================================
   EVENTS
   ===================================================== */
document.addEventListener("DOMContentLoaded", () => {

    const fySelect = document.getElementById("fySelect");
    const quarterSelect = document.getElementById("quarterSelect");

    fySelect?.addEventListener("change", loadDashboard);
    quarterSelect?.addEventListener("change", loadDashboard);

    // 🔥 carga inicial
    loadDashboard();
});
