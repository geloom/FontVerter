/*
 * Copyright (C) Maddie Abboud 2016
 *
 * FontVerter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FontVerter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FontVerter. If not, see <http://www.gnu.org/licenses/>.
 */

package org.mabb.fontverter.opentype.TtfInstructions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.control.PushNBytes;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.TtfInstruction;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;

public class TestTtfInstructionParser {
    private TtfInstructionParser parser;

    @Before
    public void init() {
        parser = new TtfInstructionParser();
    }

    @Test
    public void givenPushNBytesInstrctionWith1Byte_whenParsed_thenInstructionCodeRead() throws Exception {
        // 0x40 = code next byte is num bytes and last is the actual byte to push
        byte[] instructions = new byte[]{0x40, 0x01, 0x01};

        List<TtfInstruction> parsed = parser.parse(instructions);
        TtfInstruction pushNBytes = parsed.get(0);

        Assert.assertThat(pushNBytes, instanceOf(PushNBytes.class));
    }
}
