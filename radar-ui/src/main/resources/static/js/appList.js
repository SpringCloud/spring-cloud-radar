layui.use(['element', 'table', 'jquery', 'layer', 'form'], function () {
    var table = layui.table,
        form = layui.form,
        layer = layui.layer,
        $ = layui.$,
        element = layui.element;
    ////为app的detail.html文件中的table进行初始化
    var instanceId=$("#instanceId").val();
    var allowCross=0;
    var curPage=1;
    //第三种渲染方式：转换静态表格方式
    var tableOptions = {
        url:'/app/detail/data?instanceId='+instanceId //请求地址
    };
    //表格初始化
    table.init('appTableId', tableOptions);




    /**查询*/
    $(".appSearchList_btn").click(function () {
        getDataList($("#appName").val(),$("#appId").val(),$("#departmentName").val(),$("#ownerName").val(),1);
    });

    /**监听工具条*/
    table.on('tool(appTableId)', function (obj) {
        var data = obj.data; //获得当前行数据
        var layEvent = obj.event; //获得 lay-event 对应的值
        curPage = obj.page;
        var updateResult;
        //如果点击了更新按钮
        if (layEvent === 'servUpdate') {
            layer.open({
                title: '信息确认',
                id: data.id + 1,
                type: 1,
                content: '<div style="padding: 20px 100px;">'
                + '确认要<span class="warn1">'
                + '更新版本'
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
                        url: '/app/update/version',
                        type: 'post',
                        async: false,
                        data: {
                            "appId": data.id
                        },
                        success: function (result) {
                            updateResult = result;
                            table.reload('appTableId', {
                                where: {//请求参数
                                    appName:$("#appName").val()
                                }
                            });
                        },
                        error: function (result) {
                            updateResult=result
                        }
                    });
                    layer.closeAll();
                    if (updateResult == 'true') {
                        layer.msg("更新成功")
                    } else if(updateResult == 'false'){
                         layer.msg("更新异常")
                    }else{
                        layer.msg("会话过期，请重新登录")
                    }
                }
            });
        }


        if (layEvent === 'edit') {
            layer.open({
                type: 1,
                title:"编辑应用",
                area: ['700px', '500px'],
                content: $("#queueEditFormDiv") //这里content是一个普通的String
            });

            fillForm(data);
        }

    });

    function fillForm(app) {
        var co = $("#queueDetailForm");
        $.each(app, function (key, value) {
            co.find("input[name='"+ key +"']:not(:radio)").val(value);
            co.find("textarea[name='"+ key +"']:not(:radio)").val(value);
        });

        if(app.allowCross==1){
            co.find("input[name='allowCross'][value='"+app.allowCross+"']").prop("checked",true);
            form.render('checkbox');
        }else{
            co.find("input[name='allowCross'][value='"+app.allowCross+"']").prop("checked",false);
            form.render('checkbox');
        }


        $.ajax({
            url: '/users',
            type: 'post',
            async: false,
            success: function (data) {
                var users = [];
                if(data.data){
                    data.data.forEach(function (user) {
                        users.push({
                            id: user.userId,
                            text: user.userId+"|"+user.name
                        })});}
                $("#ownerId").select2({
                    data: users
                }).val(null).trigger("change");
                $("#memberId").select2({
                    data: users
                }).val(null).trigger("change");
            }
        });

        $.ajax({
            url: '/organizations',
            type: 'post',
            async: false,
            success: function (data) {
                var orgs = [];
                if(data.data){
                    data.data.forEach(function (org) {
                        orgs.push({
                            id: org.orgId,
                            text: org.orgName
                        })});}
                $("#departmentId").select2({
                    data: orgs
                }).val(null).trigger("change");
            }
        });
        var ownerIds=app.ownerId;
        if(ownerIds!=null&&ownerIds!=undefined){
            var ownerIdArr=ownerIds.split(",");
            $("#ownerId").val(ownerIdArr).trigger("change");
        }
        var memberIds=app.memberId;
        if(memberIds!=null&&memberIds!=undefined){
            var memberIdArr=memberIds.split(",");
            $("#memberId").val(memberIdArr).trigger("change");
        }
        $("#departmentId").val(app.departmentId).trigger("change");
        form.render();
    }
    form.on('submit(updateSubmit)', function(data){
        var ownerId;
        var memberId;
        if($("#ownerId").val()!=null&&$("#ownerId").val()!=undefined){
            ownerId=$("#ownerId").val().join(",");
        }
        if($("#memberId").val()!=null&&$("#memberId").val()!=undefined){
            memberId=$("#memberId").val().join(",")
        }
        var departmentId=$("#departmentId").val();
        $.ajax({
            url: '/app/edit',
            type: 'post',
            async: false,
            data: {
                id:data.field.id,
                candAppId:data.field.candAppId,
                appName:data.field.appName,
                domain:data.field.domain,
                ownerId:ownerId,
                memberId:memberId,
                allowCross:allowCross,
                departmentId:departmentId
            },
            success: function (data) {
                getDataList($("#appName").val(),$("#appId").val(),$("#departmentName").val(),$("#ownerName").val(),curPage);
                layer.closeAll();
                successBox(data.msg);

            },
            error: function (data) {
                failBox(data.msg);
            }
        });
    });

    function getDataList(appName,appId,departmentName,ownerName,page){
        table.reload('appTableId', {
            where: {//请求参数
                appName:appName,
                appId:appId,
                departmentName:departmentName,
                ownerName:ownerName
            },
            page:page
        });
    }

    form.on('switch(allow)', function(data){
        if(data.elem.checked){
            allowCross=1;
        }else{
            allowCross=0;
        }
    });

    function failBox(msg) {
        layer.alert(msg, {icon: 2})
    };

    function successBox(msg) {
        layer.msg(msg, {icon: 1})
    }

});