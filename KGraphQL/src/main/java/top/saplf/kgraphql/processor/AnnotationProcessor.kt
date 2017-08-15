package top.saplf.kgraphql.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.KotlinFile
import top.saplf.kgraphql.annotation.KGraph
import top.saplf.kgraphql.annotation.KGraphOperation
import top.saplf.kgraphql.annotation.KGraphQLArg
import top.saplf.kgraphql.annotation.KGraphQLName
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * @author saplf
 */
@AutoService(Processor::class) class AnnotationProcessor : AbstractProcessor() {

  private val classSet: Set<Class<out Annotation>> by lazy {
    setOf(
        KGraph::class.java,
        KGraphQLArg::class.java,
        KGraphQLName::class.java,
        KGraphOperation::class.java
    )
  }

  lateinit var mElements: Elements
  lateinit var mFiler: Filer

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    mElements = processingEnv.elementUtils
    mFiler = processingEnv.filer
  }

  override fun getSupportedAnnotationTypes() = classSet.map { it.canonicalName }.toSet()

  override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

  override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
    KotlinFile.builder("", "HelloWorld")
        .build()
        .writeTo(mFiler)
    return false
  }

  private fun KotlinFile.writeTo(filer: Filer) {
    val fileName = ""
  }
}