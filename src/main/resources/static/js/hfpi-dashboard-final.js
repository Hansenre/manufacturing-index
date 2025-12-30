// ================================
// FACTORY ID (VEM DA URL)
// /hfpi/dashboard/final/{id}
// ================================
const factoryId = window.location.pathname.split("/").pop();


let chart;

// LOAD FY
async function loadFy() {
    const res = await fetch(`/hfpi-factory/api/fy/${factoryId}`);
    const fyList = await res.json();

    const fySelect = document.getElementById("fySelect");
    fySelect.innerHTML = "";

    fyList.forEach(fy => {
        const opt = document.createElement("option");
        opt.value = fy;
        opt.text = fy;
        fySelect.appendChild(opt);
    });
}

// LOAD QUARTER
async function loadQuarter() {
    const fy = document.getElementById("fySelect").value;
    const res = await fetch(`/hfpi-factory/api/quarter/${factoryId}?fy=${fy}`);
    const quarters = await res.json();

    const quarterSelect = document.getElementById("quarterSelect");
    quarterSelect.innerHTML = "";

    quarters.forEach(q => {
        const opt = document.createElement("option");
        opt.value = q;
        opt.text = q;
        quarterSelect.appendChild(opt);
    });
}

// LOAD DASHBOARD
async function loadDashboard() {
    const fy = document.getElementById("fySelect").value;
    const quarter = document.getElementById("quarterSelect").value;

    const res = await fetch(`/hfpi/dashboard/${factoryId}?fy=${fy}&quarter=${quarter}`);
    const data = await res.json();

    renderChart(data);
}

// RENDER CHART
function renderChart(data) {
    const ctx = document.getElementById("hfpiChart");

    if (chart) chart.destroy();

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
                }
            }
        }
    });
}


// INIT
document.addEventListener("DOMContentLoaded", async () => {
    await loadFy();
    await loadQuarter();

    // garante que existe quarter selecionado
    if (document.getElementById("quarterSelect").value) {
        await loadDashboard();
    }
});

