package net.kotlinx.google.ortool

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class OrToolTest : TestRoot() {

    @Test
    fun test() {


        Loader.loadNativeLibraries()
        // Create the linear solver with the GLOP backend.
        // Create the linear solver with the GLOP backend.
        val solver: MPSolver = MPSolver.createSolver("GLOP")

        // Create the variables x and y.

        // Create the variables x and y.
        val x: MPVariable = solver.makeNumVar(0.0, 1.0, "x")
        val y: MPVariable = solver.makeNumVar(0.0, 2.0, "y")

        System.out.println("Number of variables = " + solver.numVariables())

        // Create a linear constraint, 0 <= x + y <= 2.
        val ct: MPConstraint = solver.makeConstraint(0.0, 2.0, "ct")
        ct.setCoefficient(x, 1.0)
        ct.setCoefficient(y, 1.0)

        System.out.println("Number of constraints = " + solver.numConstraints())

        // Create the objective function, 3 * x + y.

        // Create the objective function, 3 * x + y.
        val objective: MPObjective = solver.objective()
        objective.setCoefficient(x, 3.0)
        objective.setCoefficient(y, 1.0)
        objective.setMaximization()

        solver.solve()

        println("Solution:")
        System.out.println("Objective value = " + objective.value())
        System.out.println("x = " + x.solutionValue())
        System.out.println("y = " + y.solutionValue())


    }

}