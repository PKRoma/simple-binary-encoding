/*
 * Copyright 2013-2019 Real Logic Ltd.
 * Copyright (C) 2017 MarketFactory, Inc
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
package uk.co.real_logic.sbe.generation.python;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.ir.Token;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities for mapping between IR and Python
 */
public class PyUtil
{
    public static final Map<PrimitiveType, String> PRIMITIVE_TYPE_STRING_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    public static final Map<PrimitiveType, String> PRIMITIVE_TYPE_STRUCT_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    public static final Map<PrimitiveType, Integer> PRIMITIVE_TYPE_SIZE_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    public static final Map<ByteOrder, String> BYTE_ORDER_STRING_MAP = new HashMap<>();
    public static final Pattern CAM_SNAKE_PATTERN = Pattern.compile("([^_A-Z])([A-Z])");

    // Ref: https://docs.python.org/3.7/library/struct.html#format-characters
    static
    {
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.CHAR, "bytes");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.CHAR, "s");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.CHAR, 1);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT8, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT8, "b");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT8, 1);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT16, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT16, "h");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT16, 2);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT32, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT32, "i");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT32, 4);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT64, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT64, "q");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT64, 8);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT8, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT8, "B");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT8, 1);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT16, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT16, "H");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT16, 2);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT32, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT32, "I");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT32, 4);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT64, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT64, "Q");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT64, 8);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.FLOAT, "float");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.FLOAT, "f");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.FLOAT, 4);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.DOUBLE, "float");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.DOUBLE, "d");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.DOUBLE, 8);
        BYTE_ORDER_STRING_MAP.put(ByteOrder.LITTLE_ENDIAN, "<");
        BYTE_ORDER_STRING_MAP.put(ByteOrder.BIG_ENDIAN, ">");
    }

    public enum Separators
    {
        BEGIN_GROUP('['),
        END_GROUP(']'),
        BEGIN_COMPOSITE('('),
        END_COMPOSITE(')'),
        BEGIN_SET('{'),
        END_SET('}'),
        BEGIN_ARRAY('['),
        END_ARRAY(']'),
        FIELD('|'),
        KEY_VALUE('='),
        ENTRY(',');

        public final char symbol;

        Separators(final char symbol)
        {
            this.symbol = symbol;
        }

        /**
         * Add separator to a generated StringBuilder
         *
         * @param builder     the code generation builder to which information should be added
         * @param indent      the current generated code indentation
         * @param builderName of the generated StringBuilder to which separator should be added
         */
        public void appendToGeneratedBuilder(final StringBuilder builder, final String indent, final String builderName)
        {
            append(builder, indent, builderName + " += str('" + symbol + "')");
        }

        public String toString()
        {
            return String.valueOf(symbol);
        }
    }

    /**
     * Map the name of a {@link PrimitiveType} to a Python C primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Python struct primitive that most closely maps.
     */
    public static String pythonTypeName(final PrimitiveType primitiveType)
    {
        return PRIMITIVE_TYPE_STRING_ENUM_MAP.get(primitiveType);
    }

    /**
     * https://docs.python.org/3/library/struct.html
     *
     * @param primitiveType SBE primitive type
     * @return A string representation for python c struct unpacking
     */
    public static String pythonTypeCode(final PrimitiveType primitiveType)
    {
        return PRIMITIVE_TYPE_STRUCT_ENUM_MAP.get(primitiveType);
    }

    /**
     * https://docs.python.org/3/library/struct.html
     *
     * @param byteOrder byte order for convertion from Java enum to python string
     * @return python struct unpacking code for endianess
     */
    public static String pythonEndianCode(final ByteOrder byteOrder)
    {
        return BYTE_ORDER_STRING_MAP.get(byteOrder);
    }

    /**
     * Uppercase the first character of a given String.
     *
     * @param str to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toUpperFirstChar(final String str)
    {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Lowercase the first character of a given String.
     *
     * @param str to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toLowerFirstChar(final String str)
    {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Format a String as a property name.
     *
     * @param str to be formatted.
     * @return the string formatted as a property name.
     */
    public static String formatPropertyName(final String str)
    {
        return toLowerFirstChar(str);
    }

    /**
     * Format a String as a class name.
     *
     * @param str to be formatted.
     * @return the string formatted as a class name.
     */
    public static String formatClassName(final String str)
    {
        return toUpperFirstChar(str);
    }

    /**
     * Format a String as a module name.
     *
     * @param str to be formatted.
     * @return the string formatted as a class name.
     */
    public static String formatModuleName(final String str)
    {
        return camToSnake(str);
    }

    /**
     * Converts CamelCase to snake_case, which is the recommended python case
     *
     * @param str          string to transform
     * @param toUpperFirst whether to make the first char upper case first
     * @return transformed string
     */
    public static String camToSnake(final String str, final boolean toUpperFirst)
    {
        if (toUpperFirst)
        {
            return CAM_SNAKE_PATTERN.matcher(toUpperFirstChar(str)).replaceAll("$1_$2").toLowerCase();
        }
        return CAM_SNAKE_PATTERN.matcher(str).replaceAll("$1_$2").toLowerCase();
    }

    /**
     * @param str string to  transform
     * @return transformed strings  with toUpper applied
     */
    public static String camToSnake(final String str)
    {
        return camToSnake(str, true);
    }

    public static String generatePyDoc(final String indend, final Token token)
    {
        return "";
    }

    public static String generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        switch (type)
        {
            case CHAR:
                literal = "\"" + value + "\"";
                break;
            case UINT8:
            case INT8:
            case INT16:
            case UINT16:
            case UINT32:
            case INT64:
            case INT32:
                literal = value;
                break;
            case UINT64:
                literal = Long.toUnsignedString(Long.parseLong(value));
                break;
            case FLOAT:
            case DOUBLE:
                if (value.endsWith("NaN"))
                {
                    literal = "float('nan')";
                }
                else
                {
                    literal = value;
                }
                break;
        }

        return literal;
    }

    /**
     * Shortcut to append a line of generated code
     *
     * @param builder string builder to which to append the line
     * @param indent  current text indentation
     * @param line    line to be appended
     */
    public static void append(final StringBuilder builder, final String indent, final String line)
    {
        builder.append(indent).append(line).append('\n');
    }
}