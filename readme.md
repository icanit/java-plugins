Репозиторий к докладу на JPoint 2025
[Модульный монолит: как построить гибкое Java-приложение с hot reload](https://jpoint.ru/talks/cf9f633f89ea4ab9b6e2262ad332e23b/)

# Контакты для связи со мной

E-mail: icanitgs@gmail.com

Telegram: https://t.me/icanit

# Полезные ссылки
Тут указаны ссылки на ресурсы которые использовались в докладе или же могут быть полезны в контексте темы доклада.

## Доклады
- [Никита Липский — Модули Java 9. Почему не OSGi?](https://www.youtube.com/watch?v=E3A6Z02TIjg)

## Статьи:
- [Статья на Хабре про класслоадеры](https://habr.com/ru/articles/748758/)
- [Spring Boot Classloader and Class Overriding](https://dzone.com/articles/spring-boot-classloader-and-class-override)


## Документации
- [Apache Tomcat ClassLoader](https://tomcat.apache.org/tomcat-9.0-doc/class-loader-howto.html)
- [Sun GlassFish Enterprise Server Class Loader Hierarchy](https://docs.oracle.com/cd/E19879-01/821-0181/beadf/index.html)

## OSGI
- [Apache Felix](https://felix.apache.org/documentation/index.html)
- [Eclipse Equinox](https://projects.eclipse.org/projects/eclipse.equinox)
- [Knopflerfish](https://www.knopflerfish.org/)

## Фреймворки
- [Plugin Framework for Java (PF4J)](https://github.com/pf4j/pf4j)
- [Layrry - A Launcher and API for Modularized Java Applications](https://github.com/moditect/layrry)

# Про код в репозитории

Данный код собран из моей личной песочницы в рамках которой я тестировал плагинную архитектуру.
Данный реп представляет собой прототип монорепы, в которой содержатся как и родительское приложения, так и плагины.

## Основные проекты в репозитории:

### app

Тут находится springboot приложение которое позволяет запускать внутри себя инстанс плагина.
Для простоты оно поддерижвает только один инстанс плагина внутри себя, хотя ничего не мешается переписать инициализацию
с конфигурацией и запускать несколько плагинов.

в _conf/application.yml находится пример конфига этого приложения, который, в том числе, содержит настройки для запуска
плагина.
При запуске приложение вычитывает директорию с плагинами, которая в примере настроена на директорию куда gradle собирает
плагины (см. gradle таску bundlePlugin)

Приложение также поддерживает вычитку репозитория с плагинами, но для этого нужено поднять nexus/reposilite и в него
публишить собираемые плагины. (см. gradle таску publishPlugin)

### build-conventions

Тут содержатся билд скрипты для упрощения конфигурирования подпроектов в gradle

### framework

Тут содержатся разные утилиты для упрощения работы с сущностями в коде + базовые классы для плагинной системы.

### plugins-lib

Тут содержатся интерфейсы плагинов, а также интерфейсы бинов, которые ядро может инжектить в контекст плагина т.е. по
факту Plugin API и Core API

### plugins

Тут находятся подпроекты плагинов, gradle корневого проекта настроен так, что все подпроекты в этой директории сами
добавляются автоматически в корневой проект

## Как работать с песочницей:

Сначала нужно собрать плагины, вызывав:

```gradle bundlePlugin```

после чего можно запускать
``JavaPluginApp`` не забыв указать пусть к конфигу ``./_conf/application.yml``

В данном репозитории в директории ./_run хранится конфигурации запуска для IDEA для бандлинга плагинов и запуска
приложения.

Приложение содержит внутри себя swagger-ui, который доступен url: ```http://localhost:8080/swagger-ui/index.html```
после его запуска.
Через него можно дергать методы менеджера плагинов и эмулировать запрос к самому плагину и смотреть его результат.

Например:

```
curl -X 'POST' \
  'http://localhost:8080/api/plugin/action/doSomething' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d 'foo'
  ```

вернет результат:

```
{
  "data": "Bar",
  "error": null,
  "success": true
}
```

## Куда смотреть в коде:

``com.paidora.app.services.plugin.PluginsManager`` - тут по факту код отвечающий за жизненый цикл работы с плагинами.
``com.paidora.framework.modules`` - это классы базового каркаса для работы с плагинами которые уже нас стороне
приложения нужно переопределить и подтюнить под конкретную задачу.

примеры реализации плагинов в:
``com.paidora.modules.plugins.foobar.FoobarPlugin``
``com.paidora.modules.plugins.helloworld.HelloWorldPlugin``


