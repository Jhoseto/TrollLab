document.addEventListener('DOMContentLoaded', function() {
    try {
        const formattedDatesElement = document.getElementById('formattedDates');
        const commentsElement = document.getElementById('comments');

        if (!formattedDatesElement || !commentsElement) {
            console.error('Не са намерени необходимите елементи');
            return;
        }

        if (!formattedDatesElement.textContent || !commentsElement.textContent) {
            console.error('Един или повече елементи имат празно съдържание');
            return;
        }

        const labels = JSON.parse(formattedDatesElement.textContent);
        const comments = JSON.parse(commentsElement.textContent);

        if (labels.length === 0) {
            console.error('Labels са празни');
            return;
        }

        const chartData = {
            labels: labels,
            datasets: [{
                label: 'Comments',
                data: labels.map((date, index) => ({ x: date, y: index + 1 })),
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                fill: true,
                pointRadius: 6,
                pointHoverRadius: 10,
                showLine: true,
                tension: 0.4,
                borderWidth: 2,
                pointStyle: 'circle',
                pointBackgroundColor: 'rgb(255,255,255)',
                pointBorderColor: 'rgb(255,255,255)',
                pointHoverBackgroundColor: 'rgba(255, 99, 132, 1)',
                pointHoverBorderColor: 'rgba(255, 99, 132, 1)'
            }]
        };

        const config = {
            type: 'scatter',
            data: chartData,
            options: {
                scales: {
                    x: {
                        type: 'time',
                        time: {
                            unit: 'day',
                            tooltipFormat: 'yyyy-MM-dd HH:mm',
                            displayFormats: {
                                day: 'yyyy-MM-dd'
                            }
                        },
                        min: labels[0],
                        max: labels[labels.length - 1],
                        ticks: {
                            color: 'white' // Променете цвета на тиксовете на оста x
                        }
                    },
                    y: {
                        beginAtZero: true,
                        precision: 0,
                        ticks: {
                            stepSize: 1,
                            color: 'white' // Променете цвета на тиксовете на оста y
                        }
                    }
                },
                layout: {
                    padding: {
                        bottom: 100, // Допълнително място за датите и часовете
                        left: 30,
                        right: 50
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Comments Timeline',
                        font: {
                            size: 14
                        },
                        color: 'rgb(255,255,255)',
                        padding: {
                            top: 10,
                            bottom: 30
                        }
                    },
                    subtitle: {
                        display: true,
                        text: 'Graphical analysis of comments based on the timeline',
                        font: {
                            size: 16
                        },
                        color: 'rgba(255,255,255,0.8)',
                        padding: {
                            top: 0,
                            bottom: 10
                        }
                    },
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            color: 'rgb(96,208,248)'
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return comments[context.dataIndex].text;
                            }
                        }
                    }
                },
                animation: {
                    duration: 1500,
                    easing: 'easeInOutBounce'
                    
                }
            },
            plugins: [{
                id: 'customLabels',
                afterDatasetsDraw: function(chart) {
                    const ctx = chart.ctx;
                    chart.data.datasets.forEach(function(dataset) {
                        dataset.data.forEach(function(dataPoint, index) {
                            const meta = chart.getDatasetMeta(0);
                            const x = meta.data[index].x;
                            const y = chart.chartArea.bottom + 40; // Преместете датата и часа под диаграмата
                            ctx.save();
                            ctx.textAlign = 'center';
                            ctx.textBaseline = 'top';
                            ctx.fillStyle = 'white';
                            ctx.size = 10;
                            const dateLabel = new Date(dataPoint.x).toLocaleString();
                            ctx.translate(x, y); // Прехвърляне на контекста в точката
                            ctx.rotate(-Math.PI / 4.5); // Завъртане на текста на 45 градуса
                            ctx.fillText(dateLabel, 0, 0);
                            ctx.restore();
                        });
                    });
                }
            }]
        };

        const ctx = document.getElementById('commentsChart').getContext('2d');
        ctx.canvas.parentNode.style.backgroundColor = 'rgba(40,40,40,0.66)'; // Променете цвета на фона на графиката
        new Chart(ctx, config);
    } catch (error) {
        console.error('Error parsing or rendering chart data:', error);
    }
});
