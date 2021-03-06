
package org.drip.xva.trajectory;

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
 * CollateralGroupVertex holds the Vertex Realizations of a Projected Path of a Single Simulation Run along
 *  the Granularity of a Collateral Group. The References are:
 *  
 *  - Burgard, C., and M. Kjaer (2014): PDE Representations of Derivatives with Bilateral Counter-party Risk
 *  	and Funding Costs, Journal of Credit Risk, 7 (3) 1-19.
 *  
 *  - Burgard, C., and M. Kjaer (2014): In the Balance, Risk, 24 (11) 72-75.
 *  
 *  - Gregory, J. (2009): Being Two-faced over Counter-party Credit Risk, Risk 20 (2) 86-90.
 *  
 *  - Li, B., and Y. Tang (2007): Quantitative Analysis, Derivatives Modeling, and Trading Strategies in the
 *  	Presence of Counter-party Credit Risk for the Fixed Income Market, World Scientific Publishing,
 *  	Singapore.
 * 
 *  - Piterbarg, V. (2010): Funding Beyond Discounting: Collateral Agreements and Derivatives Pricing, Risk
 *  	21 (2) 97-102.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CollateralGroupVertex {
	private org.drip.analytics.date.JulianDate _dtVertex = null;
	private org.drip.xva.trajectory.CollateralGroupVertexExposure _cgve = null;
	private org.drip.xva.trajectory.CollateralGroupVertexNumeraire _cgvn = null;

	/**
	 * CollateralGroupVertex Constructor
	 * 
	 * @param dtVertex The Trade Trajectory Vertex Date
	 * @param cgve The Trade Trajectory Vertex Exposure
	 * @param cgvn The Trade Trajectory Vertex Numeraire
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralGroupVertex (
		final org.drip.analytics.date.JulianDate dtVertex,
		final org.drip.xva.trajectory.CollateralGroupVertexExposure cgve,
		final org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvn)
		throws java.lang.Exception
	{
		if (null == (_dtVertex = dtVertex) || null == (_cgve = cgve) || null == (_cgvn = cgvn))
			throw new java.lang.Exception ("CollateralGroupVertex Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Trade Trajectory Vertex Date
	 * 
	 * @return The Trade Trajectory Vertex Date
	 */

	public org.drip.analytics.date.JulianDate vertex()
	{
		return _dtVertex;
	}

	/**
	 * Retrieve the Trade Trajectory Vertex Exposure
	 * 
	 * @return The Trade Trajectory Vertex Exposure
	 */

	public org.drip.xva.trajectory.CollateralGroupVertexExposure exposure()
	{
		return _cgve;
	}

	/**
	 * Retrieve the Trade Trajectory Vertex Numeraire
	 * 
	 * @return The Trade Trajectory Vertex Numeraire
	 */

	public org.drip.xva.trajectory.CollateralGroupVertexNumeraire numeraire()
	{
		return _cgvn;
	}
}
