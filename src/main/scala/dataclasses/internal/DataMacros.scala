package dataclasses
package internal

import scala.meta._

// TODO: Add java.io.Serializable?
object DataMacros {
  def impl(defn: Stat): Stat = {
    val q"""
      ..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends { ..$earlyStats } with ..$ctorcalls {
        $selfParam =>
        ..$stats
      }
    """ = defn

    val Any           =    t"_root_.scala.Any"
    val AnyRef        =    t"_root_.scala.AnyRef"
    val AnyVal        = ctor"_root_.scala.AnyVal"
    val Boolean       =    t"_root_.scala.Boolean"
    val Product       =    t"_root_.scala.Product"
    val ProductImpl   =    q"_root_.dataclasses.ProductImpl"
    val ScalaRunTime  =    q"_root_.scala.runtime.ScalaRunTime"
    val scIterator    =    q"_root_.scala.collection.Iterator"
    val sciIndexedSeq =    q"_root_.scala.collection.immutable.IndexedSeq"

    val finalMods = mods withMod Mod.Final()

    val params = paramss.head

    val valParams  = params map (_ withMod Mod.ValParam())
    val valParamss = valParams +: paramss.tail

    val     aexprs = params map { case Term.Param(_, name @ Term.Name(_),             _, _) =>   arg"$name"                   }
    val copyParams = params map { case Term.Param(_, name @ Term.Name(_), Some(decltpe), _) => param"$name: $decltpe = $name" }
    val      withs = params map { case Term.Param(_, name @ Term.Name(_), Some(decltpe), _) =>
      val withName = Term.Name("with" + name.value.capitalize)
      q"def $withName($name: $decltpe = $name): $tname = copy($name = $name)"
    }

    def anonParam = Term.Param(Nil, Name.Anonymous(), None, None)
    val ctorref = Ctor.Ref.Name(tname.value)
    val ctorNew = Term.New(Template(Nil, List(q"$ctorref(..$aexprs)"), anonParam, None))

    val classDefn = q"""
      ..$finalMods class $tname[..$tparams] ..$ctorMods (...$valParamss) extends { ..$earlyStats } with ..$ctorcalls { $selfParam =>
        ..$stats;

        ..$withs;

        def copy(..$copyParams): $tname = $ctorNew

        override def toString = $scIterator(..$aexprs).mkString(${tname.value} + "(", ", ", ")")
        override def hashCode = $ScalaRunTime._hashCode(asProduct)
        override def equals(that: $Any) = (this eq that.asInstanceOf[$AnyRef]) || (that match {
          case that: $tname => $ScalaRunTime._equals(this.asProduct, that.asProduct)
          case _            => false
        })

        private def asProduct: $Product = $ProductImpl(${tname.value}, $sciIndexedSeq(..$aexprs))
      }
    """

    val name = Term.Name(tname.value)
    val nonValParams = params map (_ withoutMod Mod.ValParam())

    val unapplyAndExtractor = if (params.size <= 1) Nil else {
      val defDefns = params.zipWithIndex map { case (param, idx) =>
        val Term.Param(_, name @ Term.Name(_), Some(decltpe @ Type.Name(_)), _) = param
        q"def ${Term.Name(s"_${idx + 1}")}: $decltpe = x.$name"
      }
      List(
        q"def unapply(x: $tname): Extractor = new Extractor(x)",
        q"""
          final class Extractor(private val x: $tname) extends $AnyVal {
            def isEmpty: $Boolean = false
            def get: Extractor = this
            ..$defDefns
          }
        """
      )
    }

    val objectDefn = q"""
      object $name {
        def apply(..$nonValParams): $tname = $ctorNew

        ..$unapplyAndExtractor
      }
    """

    q"$classDefn; $objectDefn"
  }
}