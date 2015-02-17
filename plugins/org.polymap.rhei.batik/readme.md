# Web UI for mobile and desktop

<img align="right" width="450" style="margin-left:10px;" src="http://polymap.org/mosaic/raw-attachment/wiki/WikiStart/mosaic.png" alt=""/>

Batik is an UI framework based on Eclipse/RAP. Its focus is to provide a user centric, modern UI that is easy to programm and easy to use and navigate, both on **desktop and mobile** devices. The business and presentation logic is **written in Java**, including the entiry layout. No JavaScript involved. The system is extendible via Eclipse extension points. The appearance of the UI elements can be adjusted via **CSS** which is parsed and **validated** on the server.

## Layout: Constraint based

Layout is a main issue when designing UIs for different devices. Static layouts or, even worse, hard wiring pixels or tables does not work very well. Therefore the Batik API exposes an extendible set of layout constraints that allows the programmer to express the basic 'ideas' of the layout, like minimal/maximal size, priority, neighborhood. A constraint solver automatically generates the layout for a given display size and resolution out of theses constraints.

## Markdown

Label text can contain [Markdown](http://daringfireball.net/projects/markdown/syntax) text and/or HTML tags. Batik features the [PegDown library](https://github.com/sirthias/pegdown) to provide this functionality. This makes it very easy to format help text,

## Dashboard component

...
