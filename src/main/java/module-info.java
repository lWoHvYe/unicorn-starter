@SuppressWarnings({"requires-automatic"})
module lwohvye.unicorn.starter {
    requires lwohvye.unicorn.security;
    requires lwohvye.unicorn.tp.tools;
    requires lwohvye.unicorn.code.gen;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    // 如果使用3rd-tools，需要加入下面这两个，不清楚为何在tools中加没生效
    requires jakarta.mail;
    requires jakarta.activation;

    exports com.lwohvye.starter.modules.strategy to spring.beans; // 全是kt的package无法exports，算是bug吧

    opens config; // 注意，resources目录下的子目标并没有被open，所以需要单独open，或者直接open整个module
    opens com.lwohvye;
}
