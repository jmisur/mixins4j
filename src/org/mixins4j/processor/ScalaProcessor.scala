package org.mixins4j.processor

import java.io.OutputStream
import java.io.InputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.ArrayList
import javax.lang.model.`type`.DeclaredType
import javax.lang.model.element.AnnotationValue
import org.mixins4j.Mixin
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import org.mixins4j.writer.MixinClassWriterOptions
import scala.collection.JavaConversions._

class ScalaProcessor {
  def isMixin(m: AnnotationMirror): Boolean = {
    m.getAnnotationType().asElement().asInstanceOf[TypeElement].getQualifiedName().toString()
      .equals(classOf[Mixin].getCanonicalName())
  }

  def processAnnotationParameters(element: Element, options: MixinClassWriterOptions, processingEnv: ProcessingEnvironment) {
    element.getAnnotationMirrors().foreach(annotation =>
      if (isMixin(annotation)) {

        annotation.getElementValues.entrySet.foreach(entry =>
          {
            val key = entry.getKey
            val value = entry.getValue

            key.getSimpleName.toString match {
              case "ignoreConflicting" =>
                options.setIgnoreConflicting(value.getValue().asInstanceOf[Boolean])
              case "aggregate" =>
                options.setAggregate(value.getValue().asInstanceOf[Boolean])
              case "superclass" =>
                val superClass = value.getValue().asInstanceOf[DeclaredType].asElement().asInstanceOf[TypeElement]
                options.setSuperClass(superClass);
              case "delegates" =>
                options.setDelegates(getDelegates(value));
             // case _ => None
            }

          })
      })
  }

  def getDelegates(fieldValue: AnnotationValue): java.util.List[TypeElement] = {
    val map = fieldValue.getValue().asInstanceOf[java.util.Collection[_ <: AnnotationValue]].map(
      _.getValue().asInstanceOf[DeclaredType].asElement().asInstanceOf[TypeElement])
    new ArrayList[TypeElement](map)
  }

}
object LL {
  def main(args: Array[String]) {
    using(new FileOutputStream("xxx"), 1, 10) { x: OutputStream =>
      x.write(1)
    }
    readAll(new FileInputStream("xxx"), what)
  }

  def what(x: InputStream) {
    kak({ _(1) })
  }

  def kak(block: Array[String] => Unit) {

  }

  def using[T <: { def close(); def write(x: Int) }](stream: T, x: Int, n: Int)(block: T => Unit) {
    try {
      for (i <- 1 to n)
        stream.write(x)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      if (stream != null) stream.close()
    }
  }

  def readAll[T <: { def close(); def read(): Int }](stream: T, block: T => Unit) {
    try {
      var i = stream.read
      while (i != -1) {
        println(i)
        i = stream.read
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      if (stream != null) stream.close()
    }
  }
}