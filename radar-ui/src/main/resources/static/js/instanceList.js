var statusMsg = ["[拉入]", "[拉出]"];
var supperStatusMsg = ["下线", "禁用", "在线"];
var publishStaStyle = ["layui-form-switch", "layui-form-switch layui-form-onswitch1"];
var publishStatusMsg = ["down", "up"];
var publishIcon = ["layui-icon icon-no", "layui-icon icon-yes"];
var publishColor = ["&#x1006;", "&#xe618;"];
var supperStatusColor = ["layui-badge", "layui-badge layui-bg-gray", "layui-badge layui-bg-green"]
layui
    .use(
        ['element', 'table', 'jquery', 'layer', 'form'],
        function () {
            var table = layui.table, form = layui.form,
                layer = parent.layer === undefined ? layui.layer : parent.layer
                , $ = layui.$, element = layui.element;

            var titles = new Array()
            //定义一个参数来记录select选中项的value
            var optionVal = 0;
            //定义一个参数来记录heartSelect选中项的value
            var heartSelectVal = 0;

            //使用第三种渲染方式，为instance的list.html进行初始化
            var tag=$("#tag").val();
            var instanceListOptions={
                url: '/app/instance/list/data?tag=' + tag
            }
            //表格初始化
            table.init('instanceTableId', instanceListOptions);


            //为instanceDetail.html文件中的table进行初始化
            var instanceId = $("#instanceId").val();
            //第三种渲染方式：转换静态表格方式
            var instanceDetailOptions = {
                url: '/app/instance/detail/data?instanceId=' + instanceId //请求地址
            };
            //表格初始化
            table.init('instanceDetailTableId', instanceDetailOptions);



            //监听select选项
            form.on('select(statusSelect)', function (data) {
                optionVal = data.value
            });

            form.on('select(heartSelect)', function (data) {
                heartSelectVal = data.value
            });


            /**查询*/
            $(".servSearchList_btn").click(function () {
                getDataList(optionVal,heartSelectVal, $("#ID").val(), $("#clusterName").val(), $("#appId").val(),$("#appName").val(),$("#ip").val(),$("#sdkVersion").val(), 1);
            });

            function getDataList(optionVal,heartSelectVal, id,clusterName, appId ,appName,ip,sdkVersion,page) {
                var option={
                    where: {//请求参数
                        statusSelect: optionVal,
                        heartStatus: heartSelectVal,
                        id: id,
                        clusterName: clusterName,
                        appId: appId,
                        appName:appName,
                        ip:ip,
                        sdkVersion:sdkVersion

                    }
                };
                if(page!==undefined){
                    option["page"]=page;
                }

                table.reload('instanceTableId',option);
            }

            function refreshList() {
                getDataList(optionVal,heartSelectVal, $("#ID").val(), $("#clusterName").val(), $("#appId").val(),$("#appName").val(),$("#ip").val(),$("#sdkVersion").val());
            }


            /**监听工具条*/
            table.on(
                'tool(instanceTableId)',
                function (obj) {
                    var data = obj.data;//获得当前行数据
                    var layEvent = obj.event; //获得 lay-event 对应的值,用于监听每一行中的按钮
                    var setPublishResult;
                    var setsupperStatusResult;

                    //修改发布槽位
                    if (obj.event === 'setPublish') {
                        if (data.role == 0||data.role == 1) {
                            layer
                                .open({
                                    title: '信息确认',
                                    id: data.pubStatus + 1,
                                    type: 1,
                                    content: '<div style="padding: 20px 100px;">'
                                    + '确认要<span class="warn1">'
                                    + statusMsg[data.pubStatus]
                                    + '</span>吗？'
                                    + '</div>',
                                    btn: '确认',
                                    btnAlign: 'r' //按钮居中
                                    ,
                                    shade: 0 //不显示遮罩
                                    ,
                                    yes: function () {
                                        //这里一般是发送修改的Ajax请求
                                        $.ajax({
                                            url: '/app/instance/update/publish/status',
                                            type: 'post',
                                            async: false,
                                            data: {
                                                "pubStatus": data.pubStatus,
                                                "instanceId": data.id
                                            },
                                            success: function (data) {
                                                setPublishResult = data;
                                                refreshList();
                                            },
                                            error: function (data) {
                                                setPublishResult = data;
                                            }
                                        });

                                        layer
                                            .closeAll();
                                        if (setPublishResult == 'true') {
                                            layer.msg("修改成功")
                                        }
                                        else if(setPublishResult=='false'){
                                            layer.msg("修改失败")
                                        }else {
                                            layer.msg("会话过期，请重新登录")
                                        }
                                    }
                                });
                        }
                    }

                    //修改超级槽位
                    if (layEvent === 'supperStatusEdit') {
                        if (data.supperStatus == 0) {
                            layer
                                .open({
                                    title: '一键启停',
                                    id: data.supperStatus + 1,
                                    type: 1,
                                    content: '<div style="padding: 20px 100px;">'
                                    + '请<span class="warn1">选择</span>'
                                    + '要操作'
                                    + '的功能'
                                    + '</div>',
                                    btn: ['上线', '下线'],
                                    btnAlign: 'c' //按钮居中
                                    ,
                                    shade: 0 //不显示遮罩
                                    ,
                                    yes: function () {//点击上线按钮

                                        layer
                                            .open({
                                                title: '确认要上线吗？',
                                                id: data.supperStatus + 2,
                                                type: 1,
                                                content: '<div style="padding: 20px 100px;">'
                                                + '<span class="warn1">上线,将会接入流量</span>'
                                                + '</div>',
                                                btn: ['确认', '取消'],
                                                btnAlign: 'c' //按钮居中
                                                ,
                                                shade: 0 //不显示遮罩
                                                ,
                                                yes: function () {//点击确认
                                                    $.ajax({
                                                        url: '/app/instance/update/supper/status',
                                                        type: 'post',
                                                        async: false,
                                                        data: {
                                                            "supperStatus": '1',
                                                            "instanceId": data.id,
                                                            "originalStatus": data.supperStatus
                                                        },
                                                        success: function (data) {
                                                            setsupperStatusResult = data;
                                                            refreshList();
                                                        },
                                                        error: function (data) {
                                                            setsupperStatusResult = data;

                                                        }
                                                    });

                                                    layer
                                                        .closeAll();
                                                    if (setsupperStatusResult == 'true') {
                                                        layer.msg("修改成功")
                                                    }else if(setsupperStatusResult=='false'){
                                                        layer.msg("修改失败")
                                                    }else {
                                                        layer.msg("会话过期，请重新登录")
                                                    }
                                                },
                                                btn2: function () {
                                                    layer.closeAll()
                                                }
                                            });

                                    },
                                    btn2: function () {//点击下线按钮
                                        layer
                                            .open({
                                                title: '确认要下线吗？',
                                                id: data.supperStatus + 3,
                                                type: 1,
                                                content: '<div style="padding: 20px 100px;">'
                                                + '<span class="warn1">下线,不再接入流量</span>'
                                                + '</div>',
                                                btn: ['确认', '取消'],
                                                btnAlign: 'c' //按钮居中
                                                ,
                                                shade: 0 //不显示遮罩
                                                ,
                                                yes: function () {//点击确认

                                                    $.ajax({
                                                        url: '/app/instance/update/supper/status',
                                                        type: 'post',
                                                        async: false,
                                                        data: {
                                                            "supperStatus": '-1',
                                                            "instanceId": data.id,
                                                            "originalStatus": data.supperStatus
                                                        },
                                                        success: function (data) {
                                                            setsupperStatusResult = data;
                                                            refreshList();
                                                        },
                                                        error: function (data) {
                                                            setsupperStatusResult = data;

                                                        }
                                                    });

                                                    layer
                                                        .closeAll();
                                                    if (setsupperStatusResult == 'true') {
                                                        layer.msg("修改成功")
                                                    }else if(setsupperStatusResult=='false'){
                                                        layer.msg("修改失败")
                                                    }else {
                                                        layer.msg("会话过期，请重新登录")
                                                    }

                                                },
                                                btn2: function () {
                                                    layer.closeAll()
                                                }
                                            });
                                        return false;
                                    }
                                });
                        }
                        else if (data.supperStatus == 1) {
                            layer
                                .open({
                                    title: '一键启停',
                                    id: data.supperStatus + 1,
                                    type: 1,
                                    content: '<div style="padding: 20px 100px;">'
                                    + '请<span class="warn1">选择</span>'
                                    + '要操作'
                                    + '的功能'
                                    + '</div>',
                                    btn: ['下线', '禁用'],
                                    btnAlign: 'c' //按钮居中
                                    ,
                                    shade: 0 //不显示遮罩
                                    ,
                                    yes: function () {//点击下线按钮
                                        layer
                                            .open({
                                                title: '确认要下线吗？',
                                                id: data.supperStatus + 3,
                                                type: 1,
                                                content: '<div style="padding: 20px 100px;">'
                                                + '<span class="warn1">下线,不再接入流量</span>'
                                                + '</div>',
                                                btn: ['确认', '取消'],
                                                btnAlign: 'c' //按钮居中
                                                ,
                                                shade: 0 //不显示遮罩
                                                ,
                                                yes: function () {//点击确认

                                                    $.ajax({
                                                        url: '/app/instance/update/supper/status',
                                                        type: 'post',
                                                        async: false,
                                                        data: {
                                                            "supperStatus": '-1',
                                                            "instanceId": data.id,
                                                            "originalStatus": data.supperStatus
                                                        },
                                                        success: function (data) {
                                                            setsupperStatusResult = data;
                                                            refreshList();
                                                        },
                                                        error: function (data) {
                                                            setsupperStatusResult = data;

                                                        }
                                                    });

                                                    layer
                                                        .closeAll();
                                                    if (setsupperStatusResult == 'true') {
                                                        layer.msg("修改成功")
                                                    }else if(setsupperStatusResult=='false'){
                                                        layer.msg("修改失败")
                                                    }else {
                                                        layer.msg("会话过期，请重新登录")
                                                    }

                                                },
                                                btn2: function () {
                                                    layer.closeAll()
                                                }
                                            });
                                    },
                                    btn2: function () {
                                        layer
                                            .open({
                                                title: '确认禁用吗？',
                                                id: data.supperStatus + 3,
                                                type: 1,
                                                content: '<div style="padding: 20px 100px;">'
                                                + '确认' + '<span class="warn1">禁用</span>'
                                                + '一键启停？'
                                                + '</div>',
                                                btn: ['确认', '取消'],
                                                btnAlign: 'c' //按钮居中
                                                ,
                                                shade: 0 //不显示遮罩
                                                ,
                                                yes: function () {//点击确认

                                                    $.ajax({
                                                        url: '/app/instance/update/supper/status',
                                                        type: 'post',
                                                        async: false,
                                                        data: {
                                                            "supperStatus": '0',
                                                            "instanceId": data.id,
                                                            "originalStatus": data.supperStatus
                                                        },
                                                        success: function (data) {
                                                            setsupperStatusResult = data;
                                                            refreshList();
                                                        },
                                                        error: function (data) {
                                                            setsupperStatusResult = data;

                                                        }
                                                    });

                                                    layer
                                                        .closeAll();
                                                    if (setsupperStatusResult == 'true') {
                                                        layer.msg("修改成功")
                                                    }else if(setsupperStatusResult=='false'){
                                                        layer.msg("修改失败")
                                                    }else {
                                                        layer.msg("会话过期，请重新登录")
                                                    }

                                                },
                                                btn2: function () {
                                                    layer.closeAll()
                                                }
                                            });
                                        return false;
                                    }
                                });
                        } else if (data.supperStatus == -1) {
                            layer
                                .open({
                                    title: '一键启停',
                                    id: data.supperStatus + 1,
                                    type: 1,
                                    content: '<div style="padding: 20px 100px;">'
                                    + '请<span class="warn1">选择</span>'
                                    + '要操作'
                                    + '的功能'
                                    + '</div>',
                                    btn: ['上线', '禁用'],
                                    btnAlign: 'c' //按钮居中
                                    ,
                                    shade: 0 //不显示遮罩
                                    ,
                                    yes: function () {//点击上线按钮
                                        layer
                                            .open({
                                                title: '确认要上线吗？',
                                                id: data.supperStatus + 2,
                                                type: 1,
                                                content: '<div style="padding: 20px 100px;">'
                                                + '<span class="warn1">上线,将会接入流量</span>'
                                                + '</div>',
                                                btn: ['确认', '取消'],
                                                btnAlign: 'c' //按钮居中
                                                ,
                                                shade: 0 //不显示遮罩
                                                ,
                                                yes: function () {//点击确认
                                                    $.ajax({
                                                        url: '/app/instance/update/supper/status',
                                                        type: 'post',
                                                        async: false,
                                                        data: {
                                                            "supperStatus": '1',
                                                            "instanceId": data.id,
                                                            "originalStatus": data.supperStatus
                                                        },
                                                        success: function (data) {
                                                            setsupperStatusResult = data;
                                                            refreshList();
                                                        },
                                                        error: function (data) {
                                                            setsupperStatusResult = data;

                                                        }
                                                    });

                                                    layer
                                                        .closeAll();
                                                    if (setsupperStatusResult == 'true') {
                                                        layer.msg("修改成功")
                                                    }else if(setsupperStatusResult=='false'){
                                                        layer.msg("修改失败")
                                                    }else {
                                                        layer.msg("会话过期，请重新登录")
                                                    }
                                                },
                                                btn2: function () {
                                                    layer.closeAll()
                                                }
                                            });
                                    },
                                    btn2: function () {
                                        layer
                                            .open({
                                                title: '确认禁用吗？',
                                                id: data.supperStatus + 3,
                                                type: 1,
                                                content: '<div style="padding: 20px 100px;">'
                                                + '确认' + '<span class="warn1">禁用</span>'
                                                + '一键启停？'
                                                + '</div>',
                                                btn: ['确认', '取消'],
                                                btnAlign: 'c' //按钮居中
                                                ,
                                                shade: 0 //不显示遮罩
                                                ,
                                                yes: function () {//点击确认

                                                    $.ajax({
                                                        url: '/app/instance/update/supper/status',
                                                        type: 'post',
                                                        async: false,
                                                        data: {
                                                            "supperStatus": '0',
                                                            "instanceId": data.id,
                                                            "originalStatus": data.supperStatus
                                                        },
                                                        success: function (data) {
                                                            setsupperStatusResult = data;
                                                            refreshList();
                                                        },
                                                        error: function (data) {
                                                            setsupperStatusResult = data;

                                                        }
                                                    });

                                                    layer
                                                        .closeAll();
                                                    if (setsupperStatusResult == 'true') {
                                                        layer.msg("修改成功")
                                                    }else if(setsupperStatusResult=='false'){
                                                        layer.msg("修改失败")
                                                    }else {
                                                        layer.msg("会话过期，请重新登录")
                                                    }

                                                },
                                                btn2: function () {
                                                    layer.closeAll()
                                                }
                                            });
                                        return false;
                                    }
                                });
                        }
                    }
                    //如果点击查看图标
                    if (layEvent === 'instance_expand') {
                        var url = "/app/instance/expand?instanceId=" + data.id
                        parent.window.addTab(
                            "详情查看"+data.id, url);
                    }

                    if(layEvent==='confirm'){
                        var checkUrl="http://"+data.ip+":"+data.port+"/radar/client/instance"

                        $.ajax({
                            url: '/heart/confirm',
                            type: 'get',
                            async: false,
                            data : {
                                "checkUrl" : checkUrl
                            },
                            success: function (result) {
                                    var url="/app/heartbeatCheck?checkResult="+result+"&checkUrl="+checkUrl
                                    parent.window.addTab(
                                        "心跳确认"+data.id,url);
                            }, error: function (result) {
                                layer.msg("请求失败")
                            }
                        });
                    }

                    if(layEvent==='delete'){
                        if(data.heartStatus==0&& data.finalStatus==0){
                            layer.confirm("确定要删除该实例？", {icon: 3, title: '不可逆操作！'}, function (index) {
                                $.post("/app/instance/delete",'instanceId='+data.id, requestCallback);
                                layer.close(index);
                            });

                        }else{
                            layer.msg('不符合删除条件', {icon: 2})
                        }

                    }

                    return false;
                });

            function requestCallback(result, xhr) {
                if (xhr === 'success') {
                    if (result.code === '0') {
                        layer.msg(result.msg, {icon: 1});
                        refreshList();
                    } else {
                        layer.msg(result.msg, {icon: 2})
                    }
                } else {
                    layer.msg("网络异常！"+xhr, {icon: 2})
                }
            }


        });