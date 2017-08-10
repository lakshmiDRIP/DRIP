
package org.drip.measure.realization;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2017 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for buy/side financial/trading model
 *  	libraries targeting analysts and developers
 *  	https://lakshmidrip.github.io/DRIP/
 *  
 *  DRIP is composed of four main libraries:
 *  
 *  - DRIP Fixed Income - https://lakshmidrip.github.io/DRIP-Fixed-Income/
 *  - DRIP Asset Allocation - https://lakshmidrip.github.io/DRIP-Asset-Allocation/
 *  - DRIP Numerical Optimizer - https://lakshmidrip.github.io/DRIP-Numerical-Optimizer/
 *  - DRIP Statistical Learning - https://lakshmidrip.github.io/DRIP-Statistical-Learning/
 * 
 *  - DRIP Fixed Income: Library for Instrument/Trading Conventions, Treasury Futures/Options,
 *  	Funding/Forward/Overnight Curves, Multi-Curve Construction/Valuation, Collateral Valuation and XVA
 *  	Metric Generation, Calibration and Hedge Attributions, Statistical Curve Construction, Bond RV
 *  	Metrics, Stochastic Evolution and Option Pricing, Interest Rate Dynamics and Option Pricing, LMM
 *  	Extensions/Calibrations/Greeks, Algorithmic Differentiation, and Asset Backed Models and Analytics.
 * 
 *  - DRIP Asset Allocation: Library for model libraries for MPT framework, Black Litterman Strategy
 *  	Incorporator, Holdings Constraint, and Transaction Costs.
 * 
 *  - DRIP Numerical Optimizer: Library for Numerical Optimization and Spline Functionality.
 * 
 *  - DRIP Statistical Learning: Library for Statistical Evaluation and Machine Learning.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * UnitRandom holds the Jump Diffusion R^1 Unit Edge Realizations.
 *
 * @author Lakshmi Krishnamurthy
 */

public class UnitRandom {
	private double _dblJump = java.lang.Double.NaN;
	private double _dblDiffusion = java.lang.Double.NaN;

	/**
	 * Generate a R^1 Uniform Diffusion Realization
	 * 
	 * @return The R^1 Uniform Diffusion Realization
	 */

	public static final UnitRandom UniformDiffusion()
	{
		try {
			return new UnitRandom (java.lang.Math.random(), 0.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a R^1 Gaussian Diffusion Realization
	 * 
	 * @return The R^1 Gaussian Diffusion Realization
	 */

	public static final UnitRandom GaussianDiffusion()
	{
		try {
			return new UnitRandom (org.drip.measure.gaussian.NormalQuadrature.Random(), 0.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a R^1 Uniform Jump Realization
	 * 
	 * @return The R^1 Uniform Jump Realization
	 */

	public static final UnitRandom UniformJump()
	{
		try {
			return new UnitRandom (0., java.lang.Math.random());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a R^1 Gaussian Jump Realization
	 * 
	 * @return The R^1 Gaussian Jump Realization
	 */

	public static final UnitRandom GaussianJump()
	{
		try {
			return new UnitRandom (0., org.drip.measure.gaussian.NormalQuadrature.Random());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate an Array of R^1 Diffusion Realizations
	 * 
	 * @param adblDiffusionRealization The Array of Diffusion Realizations
	 * 
	 * @return Array of R^1 Diffusion Realizations
	 */

	public static final UnitRandom[] Diffusion (
		final double[] adblDiffusionRealization)
	{
		if (null == adblDiffusionRealization) return null;

		int iSize = adblDiffusionRealization.length;
		UnitRandom[] aJDU = 0 == iSize ? null : new UnitRandom[iSize];

		for (int i = 0; i < iSize; ++i) {
			try {
				aJDU[i] = new UnitRandom (adblDiffusionRealization[i], 0.);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aJDU;
	}

	/**
	 * Generate an Array of R^1 Jump Realizations
	 * 
	 * @param adblJumpRealization The Array of Jump Realizations
	 * 
	 * @return Array of R^1 Jump Realizations
	 */

	public static final UnitRandom[] Jump (
		final double[] adblJumpRealization)
	{
		if (null == adblJumpRealization) return null;

		int iSize = adblJumpRealization.length;
		UnitRandom[] aJDU = 0 == iSize ? null : new UnitRandom[iSize];

		for (int i = 0; i < iSize; ++i) {
			try {
				aJDU[i] = new UnitRandom (0., adblJumpRealization[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aJDU;
	}

	/**
	 * Generate an Array of R^1 Jump Diffusion Realizations
	 * 
	 * @param adblDiffusionRealization The Array of Diffusion Realizations
	 * @param adblJumpRealization The Array of Jump Realizations
	 * 
	 * @return Array of R^1 Jump Diffusion Realizations
	 */

	public static final UnitRandom[] JumpDiffusion (
		final double[] adblDiffusionRealization,
		final double[] adblJumpRealization)
	{
		if (null == adblDiffusionRealization || null == adblJumpRealization) return null;

		int iSize = adblDiffusionRealization.length;
		UnitRandom[] aJDU = 0 == iSize ? null : new UnitRandom[iSize];

		if (0 == iSize || iSize != adblJumpRealization.length) return null;

		for (int i = 0; i < iSize; ++i) {
			try {
				aJDU[i] = new UnitRandom (adblDiffusionRealization[i], adblJumpRealization[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aJDU;
	}

	/**
	 * UnitRandom Constructor
	 * 
	 * @param dblDiffusion The Diffusion Random Variable
	 * @param dblJump The Jump Random Variable
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public UnitRandom (
		final double dblDiffusion,
		final double dblJump)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDiffusion = dblDiffusion) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblJump = dblJump))
			throw new java.lang.Exception ("UnitRandom Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Diffusion Unit Random Variable
	 * 
	 * @return The Diffusion Unit Random Variable
	 */

	public double diffusion()
	{
		return _dblDiffusion;
	}

	/**
	 * Retrieve the Jump Unit Random Variable
	 * 
	 * @return The Jump Unit Random Variable
	 */

	public double jump()
	{
		return _dblJump;
	}
}
