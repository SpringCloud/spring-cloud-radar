layui.use(['layer', 'jquery', 'carousel','form'], function () {
    var $ = layui.$,
        layer = layui.layer,
        carousel = layui.carousel,
        form=layui.form;

    /**背景图片轮播*/
    carousel.render({
        elem: '#login_carousel',
        width: '100%',
        height: '100%',
        interval: 2000,
        arrow: 'none',
        anim: 'fade',
        indicator: 'none'
    });

    /**监听登陆提交*/
    form.on('submit(login)', function (data) {
        $.ajax({
            url: '/verification',
            type: 'post',
            async: false,
            data : {
                "userId" : $('#userId').val(),
                "passWord" : $('#passWord').val()
            },
            success: function (data) {
                if(data.suc==true){
                    window.location.href="/index";
                }else{
                    failBox(data.msg);
                }

            }, error: function (data) {
                failBox(data.msg);
            }
        });
        return false;
    });

    function failBox(msg) {
        layer.msg(msg, {icon: 2})
    };

    function successBox(msg) {
        layer.msg(msg, {icon: 1})
    }

});