
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
 * CollateralGroupEdgeAdjustment holds the XVA Adjustment that result from the Vertex Realizations of a
 *  Projected Path of a Single Simulation Run along the Granularity of a Collateral Group. The References
 *  are:
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

public class CollateralGroupEdgeAdjustment {
	private double _dblDebt = java.lang.Double.NaN;
	private double _dblCredit = java.lang.Double.NaN;
	private double _dblFunding = java.lang.Double.NaN;

	/**
	 * CollateralGroupEdgeAdjustment Constructor
	 * 
	 * @param dblCredit The Path-specific Credit Value Adjustment
	 * @param dblDebt The Path-specific Debt Value Adjustment
	 * @param dblFunding The Path-specific Funding Value Adjustment
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralGroupEdgeAdjustment (
		final double dblCredit,
		final double dblDebt,
		final double dblFunding)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblCredit = dblCredit) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblDebt = dblDebt) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFunding = dblFunding))
			throw new java.lang.Exception ("CollateralGroupEdgeAdjustment Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Path-specific Credit Adjustment
	 * 
	 * @return The Path-specific Credit Adjustment
	 */

	public double credit()
	{
		return _dblCredit;
	}

	/**
	 * Retrieve the Path-specific Debt Adjustment
	 * 
	 * @return The Path-specific Debt Adjustment
	 */

	public double debt()
	{
		return _dblDebt;
	}

	/**
	 * Retrieve the Path-specific Funding Adjustment
	 * 
	 * @return The Path-specific Funding Adjustment
	 */

	public double funding()
	{
		return _dblFunding;
	}
}
