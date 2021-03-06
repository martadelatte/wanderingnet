/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wanderingnet.common;

import com.google.common.annotations.Beta;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An immutable object that may contain a non-null reference to another object. Each
 * instance of this type either contains a non-null reference, or contains nothing (in
 * which case we say that the reference is "absent"); it is never said to "contain {@code
 * null}".
 *
 * <p>A non-null {@code Optional<T>} reference can be used as a replacement for a nullable
 * {@code T} reference. It allows you to represent "a {@code T} that must be present" and
 * a "a {@code T} that might be absent" as two distinct types in your program, which can
 * aid clarity.
 *
 * <p>Some uses of this class include
 *
 * <ul>
 * <li>As a method return type, as an alternative to returning {@code null} to indicate
 *     that no value was available
 * <li>To distinguish between "unknown" (for example, not present in a map) and "known to
 *     have no value" (present in the map, with value {@code Optional.absent()})
 * <li>To wrap nullable references for storage in a collection that does not support
 *     {@code null} (though there are
 *     <a href="https://github.com/google/guava/wiki/LivingWithNullHostileCollections">
 *     several other approaches to this</a> that should be considered first)
 * </ul>
 *
 * <p>A common alternative to using this class is to find or create a suitable
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> for the
 * type in question.
 *
 * <p>This class is not intended as a direct analogue of any existing "option" or "maybe"
 * construct from other programming environments, though it may bear some similarities.
 *
 * <p><b>Comparison to {@code org.stilavia.common.Optional} (JDK 8 and higher):</b> A new {@code Optional}
 * class was added for Java 8. The two classes are extremely similar, but incompatible (they cannot
 * share a common supertype). <i>All</i> known differences are listed either here or with the
 * relevant methods below.
 *
 * <ul>
 * <li>This class is serializable; {@code org.stilavia.common.Optional} is not.
 * <li>{@code org.stilavia.common.Optional} has the additional methods {@code ifPresent}, {@code filter},
 *     {@code flatMap}, and {@code orElseThrow}.
 * <li>{@code java.util} offers the primitive-specialized versions {@code OptionalInt}, {@code
 *     OptionalLong} and {@code OptionalDouble}, the use of which is recommended; Guava does not
 *     have these.
 * </ul>
 *
 * <p><b>There are no plans to deprecate this class in the foreseeable future.</b> However, we do
 * gently recommend that you prefer the new, standard Java class whenever possible.
 *
 * <p>See the Guava User Guide article on <a
 * href="https://github.com/google/guava/wiki/UsingAndAvoidingNullExplained#optional">
 * using {@code Optional}</a>.
 *
 * @param <T> the type of instance that can be contained. {@code Optional} is naturally
 *     covariant on this type, so it is safe to cast an {@code Optional<T>} to {@code
 *     Optional<S>} for any supertype {@code S} of {@code T}.
 * @author Kurt Alfred Kluever
 * @author Kevin Bourrillion
 * @since 10.0
 */
public abstract class Optional<T> implements Serializable {
    /**
     * Returns an {@code Optional} instance with no contained reference.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method is equivalent to Java 8's
     * {@code Optional.empty}.
     */
    public static <T> Optional<T> absent() {
        return Absent.withType();
    }

    public static <T> Optional<T> of(java.util.Optional<T> javaOptional) {
        if (javaOptional != null && javaOptional.isPresent()) {
            return of(javaOptional.get());
        } else {
            return absent();
        }
    }

    /**
     * Returns an {@code Optional} instance containing the given non-null reference. To have {@code
     * null} treated as {@link #absent}, use {@link #fromNullable} instead.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> no differences.
     *
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> Optional<T> of(T reference) {
        return new Present<T>(checkNotNull(reference));
    }

    public static<T> Optional<T> empty() {
        return absent();
    }

    /**
     * If {@code nullableReference} is non-null, returns an {@code Optional} instance containing that
     * reference; otherwise returns {@link Optional#absent}.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method is equivalent to Java 8's
     * {@code Optional.ofNullable}.
     */
    public static <T> Optional<T> fromNullable(T nullableReference) {
        return (nullableReference == null)
                ? Optional.<T>absent()
                : new Present<T>(nullableReference);
    }

    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    Optional() {}

    /**
     * Returns {@code true} if this holder contains a (non-null) instance.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> no differences.
     */
    public abstract boolean isPresent();

    public boolean isNotPresent() {
        return ! isPresent();
    }

    public abstract T get();

    public Optional<T> ifPresent(Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(get());
        }
        return this;
    }

    public Optional<T> ifNotPresent(Consumer<? super T> consumer) {
        if (!isPresent()) {
            consumer.accept(get());
        }
        return this;
    }

    public Optional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent())
            return this;
        else
            return predicate.test(get()) ? this : empty();
    }
    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Optional.ofNullable(mapper.apply(get()));
        }
    }

    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Objects.requireNonNull(mapper.apply(get()));
        }
    }

    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }


    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }


    /**
     * Returns the contained instance if it is present; {@code defaultValue} otherwise. If
     * no default value should be required because the instance is known to be present, use
     * {@link #get()} instead. For a default value of {@code null}, use {@link #orNull}.
     *
     * <p>Note about generics: The signature {@code public T or(T defaultValue)} is overly
     * restrictive. However, the ideal signature, {@code public <S super T> S or(S)}, is not legal
     * Java. As a result, some sensible operations involving subtypes are compile errors:
     * <pre>   {@code
     *
     *   Optional<Integer> optionalInt = getSomeOptionalInt();
     *   Number value = optionalInt.or(0.5); // error
     *
     *   FluentIterable<? extends Number> numbers = getSomeNumbers();
     *   Optional<? extends Number> first = numbers.first();
     *   Number value = first.or(0.5); // error}</pre>
     *
     * <p>As a workaround, it is always safe to cast an {@code Optional<? extends T>} to {@code
     * Optional<T>}. Casting either of the above example {@code Optional} instances to {@code
     * Optional<Number>} (where {@code Number} is the desired output type) solves the problem:
     * <pre>   {@code
     *
     *   Optional<Number> optionalInt = (Optional) getSomeOptionalInt();
     *   Number value = optionalInt.or(0.5); // fine
     *
     *   FluentIterable<? extends Number> numbers = getSomeNumbers();
     *   Optional<Number> first = (Optional) numbers.first();
     *   Number value = first.or(0.5); // fine}</pre>
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method is similar to Java 8's
     * {@code Optional.orElse}, but will not accept {@code null} as a {@code defaultValue} ({@link
     * #orNull} must be used instead). As a result, the value returned by this method is guaranteed
     * non-null, which is not the case for the {@code java.util} equivalent.
     */
    public abstract T or(T defaultValue);

    /**
     * Returns this {@code Optional} if it has a value present; {@code secondChoice}
     * otherwise.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method has no equivalent in Java 8's
     * {@code Optional} class; write {@code thisOptional.isPresent() ? thisOptional : secondChoice}
     * instead.
     */
    public abstract Optional<T> or(Optional<? extends T> secondChoice);

    /**
     * Returns the contained instance if it is present; {@code supplier.get()} otherwise.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method is similar to Java 8's
     * {@code Optional.orElseGet}, except when {@code supplier} returns {@code null}. In this case
     * this method throws an exception, whereas the Java 8 method returns the {@code null} to the
     * caller.
     *
     * @throws NullPointerException if this optional's value is absent and the supplier returns
     *     {@code null}
     */
    @Beta
    public abstract T or(Supplier<? extends T> supplier);

    /**
     * Returns the contained instance if it is present; {@code null} otherwise. If the
     * instance is known to be present, use {@link #get()} instead.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method is equivalent to Java 8's
     * {@code Optional.orElse(null)}.
     */
    public abstract T orNull();

    /**
     * Returns an immutable singleton {@link Set} whose only element is the contained instance
     * if it is present; an empty immutable {@link Set} otherwise.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method has no equivalent in Java 8's
     * {@code Optional} class. However, this common usage: <pre>   {@code
     *
     *   for (Foo foo : possibleFoo.asSet()) {
     *     doSomethingWith(foo);
     *   }}</pre>
     *
     * ... can be replaced with: <pre>   {@code
     *
     *   possibleFoo.ifPresent(foo -> doSomethingWith(foo));}</pre>
     *
     * @since 11.0
     */
    public abstract Set<T> asSet();

    /**
     * If the instance is present, it is transformed with the given {@link Function}; otherwise,
     * {@link Optional#absent} is returned.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this method is similar to Java 8's
     * {@code Optional.map}, except when {@code function} returns {@code null}. In this case this
     * method throws an exception, whereas the Java 8 method returns {@code Optional.absent()}.
     *
     * @throws NullPointerException if the function returns {@code null}
     * @since 12.0
     */
    public abstract <V> Optional<V> transform(Function<? super T, V> function);

    /**
     * Returns {@code true} if {@code object} is an {@code Optional} instance, and either
     * the contained references are {@linkplain Object#equals equal} to each other or both
     * are absent. Note that {@code Optional} instances of differing parameterized types can
     * be equal.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> no differences.
     */
    @Override
    public abstract boolean equals(Object object);

    /**
     * Returns a hash code for this instance.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this class leaves the specific choice of
     * hash code unspecified, unlike the Java 8 equivalent.
     */
    @Override
    public abstract int hashCode();

    /**
     * Returns a string representation for this instance.
     *
     * <p><b>Comparison to {@code org.stilavia.common.Optional}:</b> this class leaves the specific string
     * representation unspecified, unlike the Java 8 equivalent.
     */
    @Override
    public abstract String toString();

    private static final long serialVersionUID = 0;
}
