import arrow.core.Either
import arrow.optics.Traversal
import arrow.typeclasses.Monoid
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.arbitrary.string
import io.kotest.property.arrow.core.MonoidLaws
import io.kotest.property.arrow.core.either
import io.kotest.property.arrow.core.functionAToB
import io.kotest.property.arrow.laws.testLaws
import io.kotest.property.arrow.optics.TraversalLaws
import kotlin.jvm.JvmInline

class ExampleSpec : StringSpec({
  "true shouldBe true" {
    true shouldBe true
  }

  "exception should fail" {
    // throw RuntimeException("Boom2!")
  }

  "kotest arrow extension use-cases" {
    // smart-cast abilities for arrow types
    Either.Right("HI").shouldBeRight() shouldBe "HI"
  }

  // utilise builtin or costume Laws with Generators to verify behavior
  testLaws(
    MonoidLaws.laws(Monoid.list(), Arb.list(Arb.string())),
    MonoidLaws.laws(Monoid.numbers(), Arb.numbers())
  )

  // optics Laws from arrow
  testLaws(
    TraversalLaws.laws(
      traversal = Traversal.either(),
      aGen = Arb.either(Arb.string(), Arb.int()),
      bGen = Arb.int(),
      funcGen = Arb.functionAToB(Arb.int()),
    )
  )
})

fun Arb.Companion.numbers(): Arb<Numbers> =
  Arb.positiveInt().map { it.toNumber() }

fun Int.toNumber(): Numbers =
  if (this <= 0) Zero else Suc(minus(1).toNumber())

// natural numbers form a monoid
fun Monoid.Companion.numbers(): Monoid<Numbers> =
  object : Monoid<Numbers> {
    override fun empty(): Numbers =
      Zero

    override fun Numbers.combine(b: Numbers): Numbers =
      Suc(b)
  }

// natural numbers
sealed interface Numbers
object Zero : Numbers

@JvmInline
value class Suc(val value: Numbers) : Numbers
