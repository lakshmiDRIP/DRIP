
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
 * CollateralGroupPathAdjustment holds the Adjustments from the Exposure Sequence in a Single Path Projection
 *  Run along the Granularity of a Collateral Group. The References are:
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

public class CollateralGroupPathAdjustment {
	private double[] _adblCollateralizedExposure = null;
	private double[] _adblUncollateralizedExposure = null;
	private double[] _adblCollateralizedExposurePV = null;
	private double[] _adblUncollateralizedExposurePV = null;
	private org.drip.analytics.date.JulianDate[] _adtVertex = null;

	/**
	 * CollateralGroupPathAdjustment Constructor
	 * 
	 * @param adtVertex Array of Vertex Dates
	 * @param adblCollateralizedExposure The Array of Collateralized Exposures
	 * @param adblUncollateralizedExposure The Array of Uncollateralized Exposures
	 * @param adblCollateralizedExposurePV The Array of Collateralized Exposure PVs
	 * @param adblUncollateralizedExposurePV The Array of Uncollateralized Exposure PVs
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralGroupPathAdjustment (
		final org.drip.analytics.date.JulianDate[] adtVertex,
		final double[] adblCollateralizedExposure,
		final double[] adblUncollateralizedExposure,
		final double[] adblCollateralizedExposurePV,
		final double[] adblUncollateralizedExposurePV)
		throws java.lang.Exception
	{
		if (null == (_adtVertex = adtVertex) ||
			null == (_adblCollateralizedExposure = adblCollateralizedExposure) ||
			null == (_adblUncollateralizedExposure = adblUncollateralizedExposure) ||
			null == (_adblCollateralizedExposurePV = adblCollateralizedExposurePV) ||
			null == (_adblUncollateralizedExposurePV = adblUncollateralizedExposurePV))
			throw new java.lang.Exception ("CollateralGroupPathAdjustment Constructor => Invalid Inputs");

		int iNumEdge = _adtVertex.length;

		if (0 == iNumEdge ||
			iNumEdge != _adblCollateralizedExposure.length ||
			iNumEdge != _adblUncollateralizedExposure.length ||
			iNumEdge != _adblCollateralizedExposurePV.length ||
			iNumEdge != _adblUncollateralizedExposurePV.length ||
			!org.drip.quant.common.NumberUtil.IsValid (_adblCollateralizedExposure) ||
			!org.drip.quant.common.NumberUtil.IsValid (_adblUncollateralizedExposure) ||
			!org.drip.quant.common.NumberUtil.IsValid (_adblCollateralizedExposurePV) ||
			!org.drip.quant.common.NumberUtil.IsValid (_adblUncollateralizedExposurePV))
			throw new java.lang.Exception ("CollateralGroupPathAdjustment Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Vertex Dates
	 * 
	 * @return The Array of Vertex Dates
	 */

	public org.drip.analytics.date.JulianDate[] vertex()
	{
		return _adtVertex;
	}

	/**
	 * Retrieve the Array of Collateralized Exposures
	 * 
	 * @return The Array of Collateralized Exposures
	 */

	public double[] collateralizedExposure()
	{
		return _adblCollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposures
	 * 
	 * @return The Array of Uncollateralized Exposures
	 */

	public double[] uncollateralizedExposure()
	{
		return _adblUncollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Exposure PVs
	 * 
	 * @return The Array of Collateralized Exposure PVs
	 */

	public double[] collateralizedExposurePV()
	{
		return _adblCollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposure PVs
	 * 
	 * @return The Array of Uncollateralized Exposure PVs
	 */

	public double[] uncollateralizedExposurePV()
	{
		return _adblUncollateralizedExposurePV;
	}
}
