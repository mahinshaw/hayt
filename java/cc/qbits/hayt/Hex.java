/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Various pieces of code taken from cassandra to handle Hex encoding
// for blob fields.

package cc.qbits.hayt;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

public class Hex
{
    private static final Constructor<String> stringConstructor = getProtectedConstructor(String.class, int.class, int.class, char[].class);
    private final static byte[] charToByte = new byte[256];

    static final char[] byteToChar = new char[16];
    static
    {
        for (char c = 0; c < charToByte.length; ++c)
        {
            if (c >= '0' && c <= '9')
                charToByte[c] = (byte)(c - '0');
            else if (c >= 'A' && c <= 'F')
                charToByte[c] = (byte)(c - 'A' + 10);
            else if (c >= 'a' && c <= 'f')
                charToByte[c] = (byte)(c - 'a' + 10);
            else
                charToByte[c] = (byte)-1;
        }

        for (int i = 0; i < 16; ++i)
        {
            byteToChar[i] = Integer.toHexString(i).charAt(0);
        }
    }

    public static String bytesToHex(ByteBuffer bytes)
    {
        final int offset = bytes.position();
        final int size = bytes.remaining();
        final char[] c = new char[size * 2];
        for (int i = 0; i < size; i++)
            {
                final int bint = bytes.get(i+offset);
                c[i * 2] = byteToChar[(bint & 0xf0) >> 4];
                c[1 + i * 2] = byteToChar[bint & 0x0f];
            }
        return wrapCharArray(c);
    }

    /**
     * Create a String from a char array with zero-copy (if available), using reflection to access a package-protected constructor of String.
     * */
    public static String wrapCharArray(char[] c)
    {
        if (c == null)
            return null;

        String s = null;

        if (stringConstructor != null)
        {
            try
            {
                s = stringConstructor.newInstance(0, c.length, c);
            }
            catch (Exception e)
            {
                // Swallowing as we'll just use a copying constructor
            }
        }
        return s == null ? new String(c) : s;
    }

    /**
     * Used to get access to protected/private constructor of the specified class
     * @param klass - name of the class
     * @param paramTypes - types of the constructor parameters
     * @return Constructor if successful, null if the constructor cannot be
     * accessed
     */
    public static Constructor getProtectedConstructor(Class klass, Class... paramTypes)
    {
        Constructor c;
        try
        {
            c = klass.getDeclaredConstructor(paramTypes);
            c.setAccessible(true);
            return c;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
