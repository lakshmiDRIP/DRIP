
package org.drip.zen.linearalgebra;

/*
 * 1) Purpose, Continuity, Production Project to Show-case
 * 2) Sketch out a 2D Matrix
 * 3) Solve it on the Board
 * 4) Review the Steps
 * 5) Left/Right Construction Review => M.X = I.Y
 * 6) Symbolically Work it out
 * 7) Matrix2D Class Overview
 * 8) Note on Naming Convention
 * 9) Javadoc Generation and Review
 */

public class Matrix2D {
	private double[] _adblRHS = null;
	private double[][] _aadblCoefficient = null;

	public Matrix2D (
		final double[][] aadblCoefficient,
		final double[] adblRHS)
		throws java.lang.Exception
	{
		if (null == (_aadblCoefficient = aadblCoefficient) || null == (_adblRHS = adblRHS))
			throw new java.lang.Exception ("Matrix2D Constructor => Invalid Inputs");
	}

	public double[][] coefficients()
	{
		return _aadblCoefficient;
	}

	public double[] rhs()
	{
		return _adblRHS;
	}

	public double[] solve()
	{
		double dblDeterminant = _aadblCoefficient[0][0] * _aadblCoefficient[1][1] - _aadblCoefficient[0][1] *
			_aadblCoefficient[1][0];
		return new double[] {(_adblRHS[0] * _aadblCoefficient[1][1] - _adblRHS[1] * _aadblCoefficient[0][1]) /
			dblDeterminant, (_adblRHS[1] * _aadblCoefficient[0][0] - _adblRHS[0] * _aadblCoefficient[1][0]) /
				dblDeterminant};
	}

	public static final void main (
		final String[] input)
		throws Exception
	{
		double[][] aadblCoefficientExample = new double[][] {
			{1., 2.},
			{2., 1.}
		};
		double[] adblRHSExample = new double[] {
			5.,
			4.
		};

		Matrix2D m = new Matrix2D (
			aadblCoefficientExample,
			adblRHSExample
		);

		double[] adblSolution = m.solve();

		System.out.println (adblSolution[0] + " | " + adblSolution[1]);
	}
}
