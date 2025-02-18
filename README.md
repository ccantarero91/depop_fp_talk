# Depop Functional Programming talk

Repository to accompany my talk on Functional Programming. It contains two modules:

* Module with slides, written in markdown, with Scala code executed using [`mdoc`](https://scalameta.org/mdoc/), and presented using [`reveal`](https://revealjs.com/)
* Module with code for live coding and potentially for the examples used in the slides

## Compile slides

1. Download [`sbt`](https://www.scala-sbt.org/)
2. Run `sbt mdoc`
3. Serve the content from the `modules/talk/target/mdoc` directory, specifically the `index.html` file.
    * I usually use [`livereload`](https://github.com/lepture/python-livereload)
