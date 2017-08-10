
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
 * CounterPartyGroupAggregator aggregates across Multiple Netting Groups belonging to the Counter Party. The
 *  References are:
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

public class CounterPartyGroupAggregator {
	private org.drip.xva.trajectory.NettingGroupPathAggregator[] _aNGPA = null;

	/**
	 * CounterPartyGroupAggregator Constructor
	 * 
	 * @param aNGPA Array of Netting Group Aggregator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CounterPartyGroupAggregator (
		final org.drip.xva.trajectory.NettingGroupPathAggregator[] aNGPA)
		throws java.lang.Exception
	{
		if (null == (_aNGPA = aNGPA) || 0 == _aNGPA.length)
			throw new java.lang.Exception ("CounterPartyGroupAggregator Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Netting Group Aggregator
	 * 
	 * @return Array of Netting Group Aggregator
	 */

	public org.drip.xva.trajectory.NettingGroupPathAggregator[] nettingGroups()
	{
		return _aNGPA;
	}

	/**
	 * Retrieve the Array of the Vertex Dates
	 * 
	 * @return The Array of the Vertex Dates
	 */

	public org.drip.analytics.date.JulianDate[] vertexes()
	{
		return _aNGPA[0].vertexes();
	}

	/**
	 * Retrieve the Expected CVA
	 * 
	 * @return The Expected CVA
	 */

	public double cva()
	{
		double dblCVA = 0.;
		int iNumNettingGroup = _aNGPA.length;

		for (int i = 0; i < iNumNettingGroup; ++i)
			dblCVA += _aNGPA[i].cva();

		return dblCVA;
	}

	/**
	 * Retrieve the Expected DVA
	 * 
	 * @return The Expected DVA
	 */

	public double dva()
	{
		double dblDVA = 0.;
		int iNumNettingGroup = _aNGPA.length;

		for (int i = 0; i < iNumNettingGroup; ++i)
			dblDVA += _aNGPA[i].dva();

		return dblDVA;
	}

	/**
	 * Retrieve the Expected FCA
	 * 
	 * @return The Expected FCA
	 */

	public double fca()
	{
		double dblFCA = 0.;
		int iNumNettingGroup = _aNGPA.length;

		for (int i = 0; i < iNumNettingGroup; ++i)
			dblFCA += _aNGPA[i].fca();

		return dblFCA;
	}

	/**
	 * Retrieve the Expected Total VA
	 * 
	 * @return The Expected Total VA
	 */

	public double total()
	{
		double dblTotal = 0.;
		int iNumNettingGroup = _aNGPA.length;

		for (int i = 0; i < iNumNettingGroup; ++i)
			dblTotal += _aNGPA[i].total();

		return dblTotal;
	}

	/**
	 * Retrieve the Array of Collateralized Exposures
	 * 
	 * @return The Array of Collateralized Exposures
	 */

	public double[] collateralizedExposure()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblCollateralizedExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedExposure[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupCollateralizedExposure =
				_aNGPA[iNettingGroupIndex].collateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblCollateralizedExposure[iEdgeIndex] += adblNettingGroupCollateralizedExposure[iEdgeIndex];
		}

		return adblCollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposures
	 * 
	 * @return The Array of Uncollateralized Exposures
	 */

	public double[] uncollateralizedExposure()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblUncollateralizedExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedExposure[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupUncollateralizedExposure =
				_aNGPA[iNettingGroupIndex].uncollateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblUncollateralizedExposure[iEdgeIndex] +=
					adblNettingGroupUncollateralizedExposure[iEdgeIndex];
		}

		return adblUncollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Exposure PV's
	 * 
	 * @return The Array of Collateralized Exposure PV's
	 */

	public double[] collateralizedExposurePV()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblCollateralizedExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedExposurePV[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupCollateralizedExposurePV =
				_aNGPA[iNettingGroupIndex].collateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblCollateralizedExposurePV[iEdgeIndex] +=
					adblNettingGroupCollateralizedExposurePV[iEdgeIndex];
		}

		return adblCollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposure PV's
	 * 
	 * @return The Array of Uncollateralized Exposure PV's
	 */

	public double[] uncollateralizedExposurePV()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblUncollateralizedExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedExposurePV[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupUncollateralizedExposurePV =
				_aNGPA[iNettingGroupIndex].uncollateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblUncollateralizedExposurePV[iEdgeIndex] +=
					adblNettingGroupUncollateralizedExposurePV[iEdgeIndex];
		}

		return adblUncollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Collateralized Positive Exposures
	 * 
	 * @return The Array of Collateralized Positive Exposures
	 */

	public double[] collateralizedPositiveExposure()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblCollateralizedPositiveExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedPositiveExposure[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupCollateralizedExposure =
				_aNGPA[iNettingGroupIndex].collateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeCollateralizedExposure =
					adblNettingGroupCollateralizedExposure[iEdgeIndex];

				if (0 < dblNettingGroupEdgeCollateralizedExposure)
					adblCollateralizedPositiveExposure[iEdgeIndex] +=
						dblNettingGroupEdgeCollateralizedExposure;
			}
		}

		return adblCollateralizedPositiveExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Positive Exposure PV
	 * 
	 * @return The Array of Collateralized Positive Exposure PV
	 */

	public double[] collateralizedPositiveExposurePV()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblCollateralizedPositiveExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedPositiveExposurePV[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupCollateralizedExposurePV =
				_aNGPA[iNettingGroupIndex].collateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeCollateralizedExposurePV =
					adblNettingGroupCollateralizedExposurePV[iEdgeIndex];

				if (0 < dblNettingGroupEdgeCollateralizedExposurePV)
					adblCollateralizedPositiveExposurePV[iEdgeIndex] +=
						dblNettingGroupEdgeCollateralizedExposurePV;
			}
		}

		return adblCollateralizedPositiveExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Positive Exposures
	 * 
	 * @return The Array of Uncollateralized Positive Exposures
	 */

	public double[] uncollateralizedPositiveExposure()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblUncollateralizedPositiveExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedPositiveExposure[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupUncollateralizedExposure =
				_aNGPA[iNettingGroupIndex].uncollateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeUncollateralizedExposure =
					adblNettingGroupUncollateralizedExposure[iEdgeIndex];

				if (0 < dblNettingGroupEdgeUncollateralizedExposure)
					adblUncollateralizedPositiveExposure[iEdgeIndex] +=
						dblNettingGroupEdgeUncollateralizedExposure;
			}
		}

		return adblUncollateralizedPositiveExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Positive Exposure PV
	 * 
	 * @return The Array of Uncollateralized Positive Exposure PV
	 */

	public double[] uncollateralizedPositiveExposurePV()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblUncollateralizedPositiveExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedPositiveExposurePV[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupUncollateralizedExposurePV =
				_aNGPA[iNettingGroupIndex].uncollateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeUncollateralizedExposurePV =
					adblNettingGroupUncollateralizedExposurePV[iEdgeIndex];

				if (0 < dblNettingGroupEdgeUncollateralizedExposurePV)
					adblUncollateralizedPositiveExposurePV[iEdgeIndex] +=
						dblNettingGroupEdgeUncollateralizedExposurePV;
			}
		}

		return adblUncollateralizedPositiveExposurePV;
	}

	/**
	 * Retrieve the Array of Collateralized Negative Exposures
	 * 
	 * @return The Array of Collateralized Negative Exposures
	 */

	public double[] collateralizedNegativeExposure()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblCollateralizedNegativeExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedNegativeExposure[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupCollateralizedExposure =
				_aNGPA[iNettingGroupIndex].collateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeCollateralizedExposure =
					adblNettingGroupCollateralizedExposure[iEdgeIndex];

				if (0 > dblNettingGroupEdgeCollateralizedExposure)
					adblCollateralizedNegativeExposure[iEdgeIndex] +=
						dblNettingGroupEdgeCollateralizedExposure;
			}
		}

		return adblCollateralizedNegativeExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Negative Exposure PV
	 * 
	 * @return The Array of Collateralized Negative Exposure PV
	 */

	public double[] collateralizedNegativeExposurePV()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblCollateralizedNegativeExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedNegativeExposurePV[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupCollateralizedExposurePV =
				_aNGPA[iNettingGroupIndex].collateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeCollateralizedExposurePV =
					adblNettingGroupCollateralizedExposurePV[iEdgeIndex];

				if (0 > dblNettingGroupEdgeCollateralizedExposurePV)
					adblCollateralizedNegativeExposurePV[iEdgeIndex] +=
						dblNettingGroupEdgeCollateralizedExposurePV;
			}
		}

		return adblCollateralizedNegativeExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Negative Exposures
	 * 
	 * @return The Array of Uncollateralized Negative Exposures
	 */

	public double[] uncollateralizedNegativeExposure()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblUncollateralizedNegativeExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedNegativeExposure[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupUncollateralizedExposure =
				_aNGPA[iNettingGroupIndex].uncollateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeUncollateralizedExposure =
					adblNettingGroupUncollateralizedExposure[iEdgeIndex];

				if (0 > dblNettingGroupEdgeUncollateralizedExposure)
					adblUncollateralizedNegativeExposure[iEdgeIndex] +=
						dblNettingGroupEdgeUncollateralizedExposure;
			}
		}

		return adblUncollateralizedNegativeExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Negative Exposure PV
	 * 
	 * @return The Array of Uncollateralized Negative Exposure PV
	 */

	public double[] uncollateralizedNegativeExposurePV()
	{
		int iNumEdge = vertexes().length - 1;

		int iNumNettingGroup = _aNGPA.length;
		double[] adblUncollateralizedNegativeExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedNegativeExposurePV[j] = 0.;

		for (int iNettingGroupIndex = 0; iNettingGroupIndex < iNumNettingGroup; ++iNettingGroupIndex) {
			double[] adblNettingGroupUncollateralizedExposurePV =
				_aNGPA[iNettingGroupIndex].uncollateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblNettingGroupEdgeUncollateralizedExposurePV =
					adblNettingGroupUncollateralizedExposurePV[iEdgeIndex];

				if (0 > dblNettingGroupEdgeUncollateralizedExposurePV)
					adblUncollateralizedNegativeExposurePV[iEdgeIndex] +=
						dblNettingGroupEdgeUncollateralizedExposurePV;
			}
		}

		return adblUncollateralizedNegativeExposurePV;
	}
}
