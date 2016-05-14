file_url = ""
// myChart = echarts.init(document.getElementById('main'));

function init_monitor(){
    myChart.setOption(option)
}

function draw(path) {
    file_url = path
    executor_data = getData(file_url)
    for (var i = 0; i < 3; ++i) {
        data[i].push({
            name: executor_data.name,
            value: [executor_data.value[0],
                    executor_data.value[i+1]
                ]
        });
        console.log(data)
    }
    myChart.setOption({
        series: [{data: data[0]}, {data: data[1]}, {data: data[2]}]
    });
}

function getData(path) {
    var pa = path.split("/")
    var appId = pa[pa.length-3]
    var executorId = pa[pa.length-2]
    var url ="http://121.42.179.142:8081/getlog/?appId=" + appId + "&executorId=" + executorId
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    }else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    if(req){
        req.open("GET",url,false);
        req.send(null);
        if (req.readyState == 4) {
            if (req.status == 200) {
                response = JSON.parse(req.responseText)
                now = new Date(+now + oneTime);
                date = response.content.date
                VmHWM = response.content.VmHWM.split(' ')[0]
                VmRSS = response.content.VmRSS.split(' ')[0]
                return {
                    name: now.toString(),
                    value: [
                        now + oneTime,
                        VmRSS * 1.0,
                        (VmHWM - VmRSS) * 1.0,
                        (2048.0 - VmHWM) * 1.0,
                ]}
            };
        }
    }
}


var data = new Array(new Array(),new Array(), new Array())
var now = new Date();
var oneTime = 1000 * 60;
var value = Math.random() * 1000;
var random_data = {
            name: now.toString(),
            value: [
                now + oneTime,
                0,
                0,
                0,
            ]
        }

for (var i = 0; i <3; i++) {
        data[i].push({
            name: random_data.name,
            value: [random_data.value[0],
                    random_data.value[i+1]
                ]
        });
}


option = {
    tooltip : {
        trigger: 'axis'
    },
    legend: {
        data:['实际使用内存', '实际使用内存峰值', 'executor总内存']
    },
    toolbox: {
        feature: {
            saveAsImage: {}
        }
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis: {
        type: 'time',
        splitLine: {
            show: false
        }
    },
    yAxis: {
        type: 'value',
        boundaryGap: ['0%', '20%'],
        splitLine: {
            show: false
        }
    },
    series : [
        {
            name:'实际使用内存',
            type:'line',
            stack: '总量',
            areaStyle: {normal: {}},
            data:data[0]
        },
        {
            name:'实际使用内存峰值',
            type:'line',
            stack: '总量',
            areaStyle: {normal: {}},
            data:data[1]
        },
        {
            name:'executor总内存',
            type:'line',
            stack: '总量',
            areaStyle: {normal: {}},
            data:data[2]
        }
    ]
};


timeTicket = setInterval(function () {
    draw(file_url)
}, 1000);


// $(document).ready(function(){
// $('.kill').on('click', function () {
//   var $el = $(this);
//   var result = window.confirm('您确定kill这个executor吗?');
//   if (!result) {
//      return false;
//   }
//   var apiUrl = '/kill?' + $el.val();
//   $.ajax({
//      url: apiUrl,
//      method: 'POST',
//      data: {
//        // shop_status: $$el.val()
//      }
//     }).done(function () {
//         alert('删除成功！')
//         location = location.href;
//     }).fail(function () {
//         alert('删除失败！')
//         location = location.href;
//    });
//  });
// $('.add').on('click', function () {
//   var $el = $(this);
//   var result = window.confirm('您确定增加一个excutor么?');
//   if (!result) {
//      return false;
//   }
//   var apiUrl = '/add?worker_id=' + $el.val();
//   $.ajax({
//      url: apiUrl,
//      method: 'POST',
//      data: {
//        cores: $('#cores').val(),
//        memory: $('#memory').val()
//      }
//     }).done(function () {
//         alert('成功！')
//         location = location.href;
//     }).fail(function () {
//         alert('增加失败！')
//         location = location.href;
//    });
//  });
// });


