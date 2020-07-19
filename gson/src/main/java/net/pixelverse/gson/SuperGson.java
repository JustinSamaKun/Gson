/*
 * Copyright (C) 2008 Google Inc.
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

package net.pixelverse.gson;

import com.google.gson.*;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.pixelverse.gson.annotations.Expose;
import net.pixelverse.gson.annotations.Since;
import net.pixelverse.gson.internal.Excluder;
import net.pixelverse.gson.reflect.TypeToken;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is the main class for using Gson. Gson is typically used by first constructing a
 * Gson instance and then invoking {@link #toJson(Object)} or {@link #fromJson(String, Class)}
 * methods on it. Gson instances are Thread-safe so you can reuse them freely across multiple
 * threads.
 *
 * <p>You can create a Gson instance by invoking {@code new Gson()} if the default configuration
 * is all you need. You can also use {@link SuperGsonBuilder} to build a Gson instance with various
 * configuration options such as versioning support, pretty printing, custom
 * {@link JsonSerializer}s, {@link JsonDeserializer}s, and {@link InstanceCreator}s.</p>
 *
 * <p>Here is an example of how Gson is used for a simple Class:
 *
 * <pre>
 * Gson gson = new Gson(); // Or use new GsonBuilder().create();
 * MyType target = new MyType();
 * String json = gson.toJson(target); // serializes target to Json
 * MyType target2 = gson.fromJson(json, MyType.class); // deserializes json into target2
 * </pre></p>
 *
 * <p>If the object that your are serializing/deserializing is a {@code ParameterizedType}
 * (i.e. contains at least one type parameter and may be an array) then you must use the
 * {@link #toJson(Object, Type)} or {@link #fromJson(String, Type)} method. Here is an
 * example for serializing and deserializing a {@code ParameterizedType}:
 *
 * <pre>
 * Type listType = new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType();
 * List&lt;String&gt; target = new LinkedList&lt;String&gt;();
 * target.add("blah");
 *
 * Gson gson = new Gson();
 * String json = gson.toJson(target, listType);
 * List&lt;String&gt; target2 = gson.fromJson(json, listType);
 * </pre></p>
 *
 * <p>See the <a href="https://sites.google.com/site/gson/gson-user-guide">Gson User Guide</a>
 * for a more complete set of examples.</p>
 *
 * @see TypeToken
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @author Jesse Wilson
 */
public final class SuperGson extends Gson {

    /**
     * Constructs a Gson object with default configuration. The default configuration has the
     * following settings:
     * <ul>
     *   <li>The JSON generated by <code>toJson</code> methods is in compact representation. This
     *   means that all the unneeded white-space is removed. You can change this behavior with
     *   {@link SuperGsonBuilder#setPrettyPrinting()}. </li>
     *   <li>The generated JSON omits all the fields that are null. Note that nulls in arrays are
     *   kept as is since an array is an ordered list. Moreover, if a field is not null, but its
     *   generated JSON is empty, the field is kept. You can configure Gson to serialize null values
     *   by setting {@link SuperGsonBuilder#serializeNulls()}.</li>
     *   <li>Gson provides default serialization and deserialization for Enums, {@link Map},
     *   {@link java.net.URL}, {@link java.net.URI}, {@link java.util.Locale}, {@link java.util.Date},
     *   {@link BigDecimal}, and {@link BigInteger} classes. If you would prefer
     *   to change the default representation, you can do so by registering a type adapter through
     *   {@link SuperGsonBuilder#registerTypeAdapter(Type, Object)}. </li>
     *   <li>The default Date format is same as {@link DateFormat#DEFAULT}. This format
     *   ignores the millisecond portion of the date during serialization. You can change
     *   this by invoking {@link SuperGsonBuilder#setDateFormat(int)} or
     *   {@link SuperGsonBuilder#setDateFormat(String)}. </li>
     *   <li>By default, Gson ignores the {@link Expose} annotation.
     *   You can enable Gson to serialize/deserialize only those fields marked with this annotation
     *   through {@link SuperGsonBuilder#excludeFieldsWithoutExposeAnnotation()}. </li>
     *   <li>By default, Gson ignores the {@link Since} annotation. You
     *   can enable Gson to use this annotation through {@link SuperGsonBuilder#setVersion(double)}.</li>
     *   <li>The default field naming policy for the output Json is same as in Java. So, a Java class
     *   field <code>versionNumber</code> will be output as <code>&quot;versionNumber&quot;</code> in
     *   Json. The same rules are applied for mapping incoming Json to the Java classes. You can
     *   change this policy through {@link SuperGsonBuilder#setFieldNamingPolicy(FieldNamingPolicy)}.</li>
     *   <li>By default, Gson excludes <code>transient</code> or <code>static</code> fields from
     *   consideration for serialization and deserialization. You can change this behavior through
     *   {@link SuperGsonBuilder#excludeFieldsWithModifiers(int...)}.</li>
     * </ul>
     */
    public SuperGson() {
        this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY,
                Collections.<Type, InstanceCreator<?>>emptyMap(), DEFAULT_SERIALIZE_NULLS,
                DEFAULT_COMPLEX_MAP_KEYS, DEFAULT_JSON_NON_EXECUTABLE, DEFAULT_ESCAPE_HTML,
                DEFAULT_PRETTY_PRINT, DEFAULT_LENIENT, DEFAULT_SPECIALIZE_FLOAT_VALUES,
                LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT, DateFormat.DEFAULT,
                Collections.<TypeAdapterFactory>emptyList(), Collections.<TypeAdapterFactory>emptyList(),
                Collections.<TypeAdapterFactory>emptyList());
    }

    SuperGson(Excluder excluder, FieldNamingStrategy fieldNamingStrategy,
              Map<Type, InstanceCreator<?>> instanceCreators, boolean serializeNulls,
              boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe,
              boolean prettyPrinting, boolean lenient, boolean serializeSpecialFloatingPointValues,
              LongSerializationPolicy longSerializationPolicy, String datePattern, int dateStyle,
              int timeStyle, List<TypeAdapterFactory> builderFactories,
              List<TypeAdapterFactory> builderHierarchyFactories,
              List<TypeAdapterFactory> factoriesToBeAdded) {
        super(excluder, fieldNamingStrategy, instanceCreators, serializeNulls, complexMapKeySerialization, generateNonExecutableGson, htmlSafe, prettyPrinting, lenient, serializeSpecialFloatingPointValues, longSerializationPolicy, datePattern, dateStyle, timeStyle, builderFactories, builderHierarchyFactories, factoriesToBeAdded);
    }

    /**
     * Writes the JSON representation of {@code src} of type {@code typeOfSrc} to
     * {@code writer}.
     * @throws JsonIOException if there was a problem writing to the writer
     */
    @SuppressWarnings("unchecked")
    public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
        TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
        boolean oldLenient = writer.isLenient();
        writer.setLenient(true);
        boolean oldHtmlSafe = writer.isHtmlSafe();
        writer.setHtmlSafe(htmlSafe);
        boolean oldSerializeNulls = writer.getSerializeNulls();
        writer.setSerializeNulls(serializeNulls);
        try {
            writer.beginObject();
            writer.name("type");
            writer.value(src.getClass().getName());
            writer.name("data");
            ((TypeAdapter<Object>) adapter).write(writer, src);
            writer.endObject();
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (AssertionError e) {
            AssertionError error = new AssertionError("AssertionError : " + e.getMessage());
            error.initCause(e);
            throw error;
        } finally {
            writer.setLenient(oldLenient);
            writer.setHtmlSafe(oldHtmlSafe);
            writer.setSerializeNulls(oldSerializeNulls);
        }
    }

    public <T> T fromJson(JsonElement json) throws JsonSyntaxException, JsonIOException {
        return super.fromJson(json, (Type) null);
    }

    public <T> T fromJson(String json) throws JsonSyntaxException, JsonIOException {
        return super.fromJson(json, (Type) null);
    }

    public <T> T fromJson(Reader json) throws JsonSyntaxException, JsonIOException {
        return super.fromJson(json, (Type) null);
    }

    /**
     * Reads the next JSON value from {@code reader} and convert it to an object
     * of type {@code typeOfT}. Returns {@code null}, if the {@code reader} is at EOF.
     * Since Type is not parameterized by T, this method is type unsafe and should be used carefully
     *
     * @throws JsonIOException if there was a problem writing to the Reader
     * @throws JsonSyntaxException if json is not a valid representation for an object of type
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        boolean isEmpty = true;
        boolean oldLenient = reader.isLenient();
        reader.setLenient(true);
        try {
            reader.peek();
            isEmpty = false;
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            typeOfT = Class.forName(json.get("type").getAsString());
            reader = new JsonTreeReader(json);
            reader.beginObject();
            if (reader.nextName().equals("type")) {
                reader.nextString();
                reader.nextName();
            }
            TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfT);
            TypeAdapter<T> typeAdapter = getAdapter(typeToken);
            T object = typeAdapter.read(reader);
            while (reader.peek() != JsonToken.END_OBJECT) {
                reader.skipValue();
            }
            reader.endObject();
            return object;
        } catch (EOFException e) {
            /*
             * For compatibility with JSON 1.5 and earlier, we return null for empty
             * documents instead of throwing.
             */
            if (isEmpty) {
                return null;
            }
            throw new JsonSyntaxException(e);
        } catch (IllegalStateException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            // TODO(inder): Figure out whether it is indeed right to rethrow this as JsonSyntaxException
            throw new JsonSyntaxException(e);
        } catch (AssertionError e) {
            AssertionError error = new AssertionError("AssertionError: " + e.getMessage());
            error.initCause(e);
            throw error;
        } catch (ClassNotFoundException e) {
            throw new JsonSyntaxException(e);
        } finally {
            reader.setLenient(oldLenient);
        }
    }
}
