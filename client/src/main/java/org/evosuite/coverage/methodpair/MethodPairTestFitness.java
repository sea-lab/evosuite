package org.evosuite.coverage.methodpair;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

public class MethodPairTestFitness extends TestFitnessFunction {

    private final String className;
    private final String methodName1;
    private final String methodName2;

    public MethodPairTestFitness(String className, String methodName1, String methodName2) {
        this.className = className;
        this.methodName1 = methodName1;
        this.methodName2 = methodName2;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName1() {
        return methodName1;
    }

    public String getMethodName2() {
        return methodName2;
    }

    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;
        Set<Integer> exceptionPositions = result.getPositionsWhereExceptionsWereThrown();
        boolean haveSeenMethod1 = false;

//        System.out.println("This tests");
//        System.out.println(individual.getTestCase().toString());

        for (Statement stmt : result.test) {
            if ((stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)) {
                EntityWithParametersStatement ps = (EntityWithParametersStatement) stmt;
                String className = ps.getDeclaringClassName();
                String methodName = ps.getMethodName() + ps.getDescriptor();

                if (haveSeenMethod1) {
                    if (this.className.equals(className) && this.methodName2.equals(methodName)) {
                        fitness = 0.0;
                        break;
                    }
                } else if (this.className.equals(className) && this.methodName1.equals(methodName)) {
                    haveSeenMethod1 = true;
                    fitness = 0.5;
                } else {
                    haveSeenMethod1 = false;
                }
            }
            if (exceptionPositions.contains(stmt.getPosition()))
                break;
        }
        updateIndividual(this, individual, fitness);
        return fitness;
    }

    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof MethodPairTestFitness) {
            MethodPairTestFitness otherMethodFitness = (MethodPairTestFitness) other;
            if (className.equals(otherMethodFitness.getClassName())) {
                if(methodName1.equals(otherMethodFitness.getMethodName1()))
                    return methodName2.compareTo(otherMethodFitness.getMethodName2());
                else
                    return methodName1.compareTo(otherMethodFitness.getMethodName1());
            }
            else
                return className.compareTo(otherMethodFitness.getClassName());
        }
        return compareClassName(other);
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName1 != null ? methodName1.hashCode() : 0);
        result = 31 * result + (methodName2 != null ? methodName2.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodPairTestFitness that = (MethodPairTestFitness) o;

        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (methodName1 != null ? !methodName1.equals(that.methodName1) : that.methodName1 != null) return false;
        return methodName2 != null ? methodName2.equals(that.methodName2) : that.methodName2 == null;
    }

    @Override
    public String getTargetClass() {
        return null;
    }

    @Override
    public String getTargetMethod() {
        return null;
    }
}
