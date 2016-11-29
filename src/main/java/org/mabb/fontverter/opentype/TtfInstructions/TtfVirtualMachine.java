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

import org.mabb.fontverter.io.FontDataInputStream;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.*;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.control.IfInstruction;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.control.ElseInstruction;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.control.EndFunctionInstruction;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.control.EndIfInstruction;
import org.mabb.fontverter.opentype.TtfInstructions.instructions.control.FunctionDefInstruction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class TtfVirtualMachine implements TtfInstructionVisitor {
    public int jumpOffset = 0;

    private InstructionStack stack;
    private final FontDataInputStream fontInput;
    private Stack<IfInstruction> ifStack = new Stack<IfInstruction>();
    private int discardIf = 0;

    private TtfFunction functionOn;
    private Map<Integer, TtfFunction> functions = new HashMap<Integer, TtfFunction>();

    public TtfVirtualMachine(FontDataInputStream fontInput) {
        this.fontInput = fontInput;
        this.stack = new InstructionStack();
    }

    public void execute(List<TtfInstruction> instructions) throws IOException {
        for (int i = 0; i < instructions.size(); i++) {
            TtfInstruction instructionOn = instructions.get(i);
            instructionOn.vm = this;
            execute(instructionOn);

            if (jumpOffset > 0) {
                i += jumpOffset - 1;
                jumpOffset = 0;
            }
        }
    }

    public void execute(TtfInstruction instruction) throws IOException {
        instruction.accept(this);
    }

    public void visitGeneric(TtfInstruction instruction) throws IOException {
        if (!shouldExecuteBranch())
            return;

        if (functionOn == null)
            instruction.execute(fontInput, stack);
        else
            // since functions get their unique ID from the stack we must build the functions
            // at run time rather then in the parse stage
            functionOn.addInstruction(instruction);
    }

    public void visit(IfInstruction instruction) throws IOException {
        if (!shouldExecuteBranch()) {
            discardIf++;
            return;
        }

        instruction.execute(fontInput, stack);
        ifStack.push(instruction);
    }

    public void visit(ElseInstruction instruction) throws IOException {
        if (ifStack.size() == 0)
            throw new TtfVmRuntimeException("Else with no matching If!!");

        ifStack.peek().shouldExecute = !ifStack.peek().shouldExecute;
    }

    public void visit(EndIfInstruction instruction) throws IOException {
        if (discardIf > 0) {
            discardIf--;
            return;
        }

        if (ifStack.size() == 0)
            throw new TtfVmRuntimeException("End If with no matching If!!");

        ifStack.pop();
    }

    public void visit(FunctionDefInstruction instruction) throws IOException {
        instruction.execute(fontInput, stack);

        functionOn = new TtfFunction();
        functions.put(instruction.getFunctionId(), functionOn);
    }

    public void visit(EndFunctionInstruction instruction) throws IOException {
        if (functionOn == null)
            throw new TtfVmRuntimeException("End function with no matching func def start!!");

        functionOn = null;
    }

    public boolean shouldExecuteBranch() {
        if (ifStack.size() == 0)
            return true;

        return ifStack.get(ifStack.size() - 1).shouldExecute;
    }

    public TtfFunction getFunction(Integer function) {
        return functions.get(function);
    }

    InstructionStack getStack() {
        return stack;
    }

    public class TtfVmRuntimeException extends IOException {
        public TtfVmRuntimeException(String message) {
            super(message);
        }
    }
}
