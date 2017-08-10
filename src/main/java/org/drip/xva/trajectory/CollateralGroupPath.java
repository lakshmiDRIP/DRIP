
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
 * CollateralGroupPath accumulates the Vertex Realizations of the Sequence in a Single Path Projection Run
 *  along the Granularity of a Collateral Group. The References are:
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

public class CollateralGroupPath {
	private org.drip.xva.trajectory.CollateralGroupEdge[] _aCGE = null;

	/**
	 * CollateralGroupPath Constructor
	 * 
	 * @param aCGE The Array of Netting Group Trajectory Edges
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralGroupPath (
		final org.drip.xva.trajectory.CollateralGroupEdge[] aCGE)
		throws java.lang.Exception
	{
		if (null == (_aCGE = aCGE))
			throw new java.lang.Exception ("CollateralGroupPath Constructor => Invalid Inputs");

		int iNumEdge = _aCGE.length;

		if (1 >= iNumEdge)
			throw new java.lang.Exception ("CollateralGroupPath Constructor => Invalid Inputs");

		for (int i = 0; i < iNumEdge; ++i) {
			if (null == _aCGE[i])
				throw new java.lang.Exception ("CollateralGroupPath Constructor => Invalid Inputs");

			if (0 != i && _aCGE[i - 1].tail().vertex().julian() != _aCGE[i].head().vertex().julian())
				throw new java.lang.Exception ("CollateralGroupPath Constructor => Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Array of Netting Group Trajectory Edges
	 * 
	 * @return The Array of Netting Group Trajectory Edges
	 */

	public org.drip.xva.trajectory.CollateralGroupEdge[] edges()
	{
		return _aCGE;
	}

	/**
	 * Retrieve the Path-wise Credit Adjustment
	 * 
	 * @return The Path-wise Credit Adjustment
	 */

	public double credit()
	{
		double dblCredit = 0.;

		for (org.drip.xva.trajectory.CollateralGroupEdge cge : _aCGE)
			dblCredit += cge.credit();

		return dblCredit;
	}

	/**
	 * Retrieve the Path-wise Debt Adjustment
	 * 
	 * @return The Path-wise Debt Adjustment
	 */

	public double debt()
	{
		double dblDebt = 0.;

		for (org.drip.xva.trajectory.CollateralGroupEdge cge : _aCGE)
			dblDebt += cge.debt();

		return dblDebt;
	}

	/**
	 * Retrieve the Path-wise Funding Adjustment
	 * 
	 * @return The Path-wise Funding Adjustment
	 */

	public double funding()
	{
		double dblFunding = 0.;

		for (org.drip.xva.trajectory.CollateralGroupEdge cge : _aCGE)
			dblFunding += cge.funding();

		return dblFunding;
	}

	/**
	 * Retrieve the Path-wise Total Adjustment
	 * 
	 * @return The Path-wise Total Adjustment
	 */

	public double total()
	{
		double dblTotal = 0.;

		for (org.drip.xva.trajectory.CollateralGroupEdge cge : _aCGE)
			dblTotal += cge.total();

		return dblTotal;
	}

	/**
	 * Retrieve the Array of Collateralized Exposures
	 * 
	 * @return The Array of Collateralized Exposures
	 */

	public double[] collateralizedExposure()
	{
		int iNumEdge = _aCGE.length;
		double[] adblCollateralizedExposure = new double[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i)
			adblCollateralizedExposure[i] = _aCGE[i].tail().exposure().collateralized();

		return adblCollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposures
	 * 
	 * @return The Array of Uncollateralized Exposures
	 */

	public double[] uncollateralizedExposure()
	{
		int iNumEdge = _aCGE.length;
		double[] adblUncollateralizedExposure = new double[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i)
			adblUncollateralizedExposure[i] = _aCGE[i].tail().exposure().uncollateralized();

		return adblUncollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Exposure PVs
	 * 
	 * @return The Array of Collateralized Exposure PVs
	 */

	public double[] collateralizedExposurePV()
	{
		int iNumEdge = _aCGE.length;
		double[] adblCollateralizedExposurePV = new double[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i) {
			org.drip.xva.trajectory.CollateralGroupVertex cgvTail =_aCGE[i].tail();

			adblCollateralizedExposurePV[i] = cgvTail.exposure().collateralized() /
				cgvTail.numeraire().csa();
		}

		return adblCollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposure PVs
	 * 
	 * @return The Array of Uncollateralized Exposure PVs
	 */

	public double[] uncollateralizedExposurePV()
	{
		int iNumEdge = _aCGE.length;
		double[] adblUncollateralizedExposurePV = new double[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i) {
			org.drip.xva.trajectory.CollateralGroupVertex cgvTail =_aCGE[i].tail();

			adblUncollateralizedExposurePV[i] = cgvTail.exposure().uncollateralized() /
				cgvTail.numeraire().csa();
		}

		return adblUncollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Collateral Balances
	 * 
	 * @return The Array of Collateral Balances
	 */

	public double[] collateralBalance()
	{
		int iNumEdge = _aCGE.length;
		double[] adblCollateralizedBalance = new double[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i)
			adblCollateralizedBalance[i] = _aCGE[i].tail().exposure().collateralBalance();

		return adblCollateralizedBalance;
	}

	/**
	 * Retrieve the Array of Edge Adjustments
	 * 
	 * @return The Array of Edge Adjustments
	 */

	public org.drip.xva.trajectory.CollateralGroupEdgeAdjustment[] edgeAdjustments()
	{
		int iNumEdge = _aCGE.length;
		org.drip.xva.trajectory.CollateralGroupEdgeAdjustment[] aCGEA = new
			org.drip.xva.trajectory.CollateralGroupEdgeAdjustment[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i)
			aCGEA[i] = _aCGE[i].generate();

		return aCGEA;
	}

	/**
	 * Construct the Group Trajectory Path Adjustment Instance
	 * 
	 * @return The Group Trajectory Path Adjustment Instance
	 */

	public org.drip.xva.trajectory.CollateralGroupPathAdjustment adjustment()
	{
		int iNumEdge = _aCGE.length;
		double[] adblCollateralizedExposure = new double[iNumEdge];
		double[] adblUncollateralizedExposure = new double[iNumEdge];
		double[] adblCollateralizedExposurePV = new double[iNumEdge];
		double[] adblUncollateralizedExposurePV = new double[iNumEdge];
		org.drip.analytics.date.JulianDate[] adtVertex = new org.drip.analytics.date.JulianDate[iNumEdge];

		for (int i = 0; i < iNumEdge; ++i) {
			org.drip.xva.trajectory.CollateralGroupVertex cgvTail =_aCGE[i].tail();

			adtVertex[i] = cgvTail.vertex();

			double dblTailCSANumeraire = cgvTail.numeraire().csa();

			org.drip.xva.trajectory.CollateralGroupVertexExposure cgve = cgvTail.exposure();

			adblUncollateralizedExposure[i] = cgve.uncollateralized();

			adblCollateralizedExposurePV[i] = (adblCollateralizedExposure[i] = cgve.collateralized()) /
				dblTailCSANumeraire;

			adblUncollateralizedExposurePV[i] = (adblUncollateralizedExposure[i] = cgve.collateralized()) /
				dblTailCSANumeraire;
		}

		try {
			return new org.drip.xva.trajectory.CollateralGroupPathAdjustment (
				adtVertex,
				adblCollateralizedExposure,
				adblUncollateralizedExposure,
				adblCollateralizedExposurePV,
				adblUncollateralizedExposurePV
			);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
