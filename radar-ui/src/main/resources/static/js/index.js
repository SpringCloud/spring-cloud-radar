layui.use(['element', 'layer', 'jquery'], function () {
    var layer = layui.layer
        , $ = layui.$
        , element = layui.element;
    //titles用于存储新建tab的名字, href为新建tab的跳转路径, delIndex为删除的tab在数组中的下标
    var titles = new Array(), href, delIndex;

    //根据用户角色判断是否显示trace导航栏，只有超级管理员才可以查看trace,(0表示超级管理员角色)
    $(function () {
        function hide() {
            //默认不显示trace导航
                //获取角色
                $.ajax({
                    url: '/app/hide',
                    type: 'POST',
                    async: false,
                    success: function (data) {
                        if(data==0){
                            $("#hide").show();
                        }else{
                            $("#hide").hide();
                        }
                    },
                    error: function (data) {
                    }
                });
        }
        hide();
    });


    //触发事件,用于被子页面调用
    function addTab(id, url) {
        if ($.inArray(id, titles) > -1) {
            element.tabChange('bodyTab', id);
        } else {
            titles.push(id)
            element.tabAdd('bodyTab', {
                title: id,
                content: '<iframe src="' + url + '"></iframe>',
                id: id
            });
            element.tabChange('bodyTab', id);
        }
    }
    window.addTab = addTab;



    //监听左侧导航
    element.on('nav(test)', function (elem) {
        href = $(this).find("a").attr("data-url");
        //判断titles数组中是否存在该id
        if ($.inArray($(this).find("a").attr("id"), titles) > -1) {
            element.tabChange('bodyTab', $(this).find("a").attr("id"));//如果该tab已经打开,则展示该tab
        } else {//如果不存在则把该id添加到数组titles中，新增tab并且展示
            titles.push($(this).find("a").attr("id"))
            element.tabAdd('bodyTab', {
                title: elem.text(),
                content: '<iframe src="' + href + '"></iframe>',
                id: $(this).find("a").attr("id")
            });
            element.tabChange('bodyTab', $(this).find("a").attr("id"));
        }
    });
    //tab的删除
    element.on('tabDelete(bodyTab)', function (data) {
        delIndex = data.index - 1;
        titles.splice(delIndex, 1);
    });
});