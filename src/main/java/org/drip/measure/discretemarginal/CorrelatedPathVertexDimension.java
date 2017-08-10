
package org.drip.measure.discretemarginal;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2017 Lakshmi Krishnamurthy
 * Copyright (C) 2016 Lakshmi Krishnamurthy
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * CorrelatedPathVertexDimension generates Correlated R^d Random Numbers at the specified Vertexes, over the
 * 	Specified Paths.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CorrelatedPathVertexDimension {
	private int _iNumPath = -1;
	private int _iNumVertex = -1;
	private boolean _bApplyAntithetic = false;
	private double[][] _aadblCorrelation = null;
	private org.drip.measure.crng.RandomNumberGenerator _rng = null;

	/**
	 * CorrelatedPathVertexDimension Constructor
	 * 
	 * @param rng The Random Number Generator
	 * @param aadblCorrelation The Correlation Matrix
	 * @param iNumVertex Number of Vertexes
	 * @param iNumPath Number of Paths
	 * @param bApplyAntithetic TRUE - Apply Antithetic Variables Based Variance Reduction
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CorrelatedPathVertexDimension (
		final org.drip.measure.crng.RandomNumberGenerator rng,
		final double[][] aadblCorrelation,
		final int iNumVertex,
		final int iNumPath,
		final boolean bApplyAntithetic)
		throws java.lang.Exception
	{
		if (null == (_rng = rng) || null == (_aadblCorrelation = aadblCorrelation) || 0 >= (_iNumVertex =
			iNumVertex) || 0 >= (_iNumPath = iNumPath))
			throw new java.lang.Exception ("CorrelatedPathVertexDimension Constructor => Invalid Inputs");

		_bApplyAntithetic = bApplyAntithetic;
		int iDimension = _aadblCorrelation.length;

		if (0 == iDimension)
			throw new java.lang.Exception ("CorrelatedPathVertexDimension Constructor => Invalid Inputs");

		for (int i = 0; i < iDimension; ++i) {
			if (null == _aadblCorrelation[i] || iDimension != _aadblCorrelation[i].length ||
				!org.drip.quant.common.NumberUtil.IsValid (_aadblCorrelation[i]))
				throw new java.lang.Exception
					("CorrelatedPathVertexDimension Constructor => Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Random Number Generator
	 * 
	 * @return The Random Number Generator Instance
	 */

	public org.drip.measure.crng.RandomNumberGenerator randomNumberGenerator()
	{
		return _rng;
	}

	/**
	 * Retrieve the Correlation Matrix
	 * 
	 * @return The Correlation Matrix
	 */

	public double[][] correlation()
	{
		return _aadblCorrelation;
	}

	/**
	 * Retrieve the Number of Vertexes
	 * 
	 * @return The Number of Vertexes
	 */

	public int numVertex()
	{
		return _iNumVertex;
	}

	/**
	 * Retrieve the Number of Paths
	 * 
	 * @return The Number of Paths
	 */

	public int numPath()
	{
		return _iNumPath;
	}

	/**
	 * Retrieve the Number of Dimensions
	 * 
	 * @return The Number of Dimensions
	 */

	public int numDimension()
	{
		return _aadblCorrelation.length;
	}

	/**
	 * Indicate if the Antitehtic Variable Generation is to be applied
	 * 
	 * @return TRUE - Apply Antithetic Variables Based Variance Reduction
	 */

	public boolean applyAntithetic()
	{
		return _bApplyAntithetic;
	}
}
