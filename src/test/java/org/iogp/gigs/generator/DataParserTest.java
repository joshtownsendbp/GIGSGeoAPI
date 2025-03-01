/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.iogp.gigs.generator;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the {@link DataParser} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class DataParserTest {
    /**
     * Tests {@link DataParser#parseRow(String, Class[])}.
     *
     * @throws IOException if an error occurred while parsing the row.
     */
    @Test
    @SuppressWarnings("UnnecessaryBoxing")
    public void testParseRow() throws IOException {
        final Object[] values = DataParser.parseRow("8901\ttrue\tGreenwich\t\t\"0°\"\tsexagesimal degree\t0",
            Integer.class, Boolean.class, String.class, String.class, String.class, String.class, Double.class);

        assertEquals(Integer.valueOf(8901), values[0], "EPSG Prime Meridian Code");
        assertEquals(Boolean.TRUE,          values[1], "Particularly important to E&P industry?");
        assertEquals("Greenwich",           values[2], "EPSG Prime Meridian Name");
        assertEquals(null,                  values[3], "EPSG Alias");
        assertEquals("0°",                  values[4], "Longitude from Greenwich (sexagesimal)");
        assertEquals("sexagesimal degree",  values[5], "Unit Name");
        assertEquals(Double.valueOf(0.0),   values[6], "Longitude from Greenwich (degrees)");
    }
}
