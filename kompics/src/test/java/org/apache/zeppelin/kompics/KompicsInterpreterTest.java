/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.kompics;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class KompicsInterpreterTest {

	private static KompicsInterpreter kompicspreter;
	private static InterpreterContext context;

	@BeforeClass
	public static void setUp() {
		Properties p = new Properties();
		kompicspreter = new KompicsInterpreter(p);
		try {
			kompicspreter.open();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		context = new InterpreterContext(null, null, null, null, null, null, null, null, null, null, null);
	}

	@AfterClass
	public static void tearDown() {
		kompicspreter.close();
		kompicspreter.destroy();
	}

	@Test
	public void testNextLineInvocation() {
		assertEquals(InterpreterResult.Code.SUCCESS, kompicspreter.interpret("\"123\"\n.toInt", context).code());
	}

	@Test
	public void testNextLineComments() {
		assertEquals(InterpreterResult.Code.SUCCESS, kompicspreter.interpret("\"123\"\n/*comment here\n*/.toInt", context).code());
	}

	@Test
	public void testNextLineCompanionObject() {
		String code = "class Counter {\nvar value: Long = 0\n}\n // comment\n\n object Counter {\n def apply(x: Long) = new Counter()\n}";
		assertEquals(InterpreterResult.Code.SUCCESS, kompicspreter.interpret(code, context).code());
	}

	@Test
	public void testSimpleStatement() {
		kompicspreter.interpret("val a=1", context);
		InterpreterResult result = kompicspreter.interpret("print(a)", context);
		assertEquals("1", result.message());
	}

	@Test
	public void testSimpleStatementWithSystemOutput() {
		kompicspreter.interpret("val a=1", context);
		InterpreterResult result = kompicspreter.interpret("System.out.print(a)", context);
		assertEquals("1", result.message());
	}

	@Test
	public void testKompicsComponents() {

		InterpreterResult result = kompicspreter.interpret(
				"class HelloWorldC extends ComponentDefinition {\n" +
				"    ctrl uponEvent {\n" +
				"        case _: Start => handle {\n" +
				"            println(\"Hello World!\");\n" +
				"            Kompics.asyncShutdown();\n" +
				"        }\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"Kompics.createAndStart(classOf[HelloWorldC])", context);

		assertEquals(InterpreterResult.Code.SUCCESS, result.code());
//
//		String[] expectedCounts = {"(to,2)", "(be,2)", "(or,1)", "(not,1)"};
//		Arrays.sort(expectedCounts);
//
//		String[] counts = result.message().split("\n");
//		Arrays.sort(counts);
//
//		assertArrayEquals(expectedCounts, counts);
	}
}
