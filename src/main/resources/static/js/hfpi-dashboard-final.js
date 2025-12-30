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
	updateChartTitle();
	

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
			    backgroundColor: ['#1976d2', '#ef6c00', '#2e7d32'],
			    barThickness: 80,       // 👈 barras mais grossas
			    borderRadius: 6
			}]

        },
		options: {
		    indexAxis: 'y',
		    responsive: true,
		    maintainAspectRatio: false,

			layout: {
			    padding: {
			        left: 60,   // espaço para labels HFPI
			        right: 90,  // espaço para % + Bronze/Prata
			        top: 20,
			        bottom: 20
			    }
			},

		    scales: {
		        x: {
		            min: 0,
		            max: 100,
		            grid: {
		                color: '#e0e0e0'
		            },
		            ticks: {
		                callback: v => v + '%',
		                font: { size: 11 }
		            }
		        },
		        y: {
		            ticks: {
		                font: {
		                    size: 12,
		                    weight: 'bold'
		                },
		                color: '#333'
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
		            offset: 6,
		            formatter: v => v.toFixed(1) + '%',
		            color: '#111',
		            font: {
		                weight: 'bold',
		                size: 11
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
				                xAdjust: 8,
				                yAdjust: -12,
				                backgroundColor: '#cd7f32',
				                color: '#fff',
				                font: { size: 12 }
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
				                xAdjust: 8,
				                yAdjust: 12,
				                backgroundColor: '#c0c0c0',
				                color: '#000',
				                font: { size: 12 }
				            }
				        }
				    }
				}

		    }
		}

    });
}

function exportPdf() {

    const element = document.getElementById("pdf-content");

    const opt = {
        margin:       0.5,
        filename:     `HFPI_Final_${new Date().toISOString().slice(0,10)}.pdf`,
        image:        { type: 'jpeg', quality: 0.98 },
        html2canvas:  {
            scale: 2,
            useCORS: true
        },
        jsPDF: {
            unit: 'cm',
            format: 'a4',
            orientation: 'landscape'
        }
    };

    html2pdf()
        .set(opt)
        .from(element)
        .save();
}

function updateChartTitle() {
    const factorySelect = document.getElementById("factorySelect");
    const fySelect = document.getElementById("fySelect");
    const quarterSelect = document.getElementById("quarterSelect");

    const factoryName =
        factorySelect?.options[factorySelect.selectedIndex]?.text || "Factory";

    const fy = fySelect?.value || "";
    const quarter = quarterSelect?.value || "";

    const title = `HFPI 2.0 – ${factoryName} | ${fy} ${quarter}`;

    document.getElementById("chartTitle").innerText = title;
}

function onFactoryChange() {
    const factoryId = document.getElementById("factorySelect").value;
    window.location.href = `/hfpi/dashboard/final/${factoryId}`;
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
