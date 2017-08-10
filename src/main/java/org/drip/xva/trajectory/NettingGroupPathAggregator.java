
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
 * NettingGroupPathAggregator aggregates across Multiple Path Projection Runs along the Granularity of a
 *  Netting Group. The References are:
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

public class NettingGroupPathAggregator {
	private org.drip.xva.trajectory.NettingGroupPath[] _aNGP = null;

	/**
	 * Construct a Standard NettingGroupPathAggregator Instance
	 * 
	 * @param adtVertex Array of the Evolution Vertex Dates
	 * @param aadblPortfolioValue Array of the Portfolio Path Value Realizations
	 * @param aadblCollateralBalance Array of Path Collateral Balances
	 * @param aaCGVN Array of the GroupTrajectoryVertexNumeraire Realizations
	 * 
	 * @return The Standard NettingGroupPathAggregator Instance
	 */

	public static final NettingGroupPathAggregator Standard (
		final org.drip.analytics.date.JulianDate[] adtVertex,
		final double[][] aadblPortfolioValue,
		final double[][] aadblCollateralBalance,
		final org.drip.xva.trajectory.CollateralGroupVertexNumeraire[][] aaCGVN)
	{
		if (null == adtVertex || null == aadblPortfolioValue || null == aadblCollateralBalance || null ==
			aaCGVN)
			return null;

		int iNumSimulation = aadblPortfolioValue.length;

		if (0 == iNumSimulation || iNumSimulation != aadblPortfolioValue.length || iNumSimulation !=
			aadblCollateralBalance.length || null == aadblPortfolioValue[0] || null ==
				aadblCollateralBalance[0] || null == aaCGVN[0])
			return null;

		int iNumTimeStep = aadblPortfolioValue[0].length;
		org.drip.xva.trajectory.CollateralGroupPath[] aCGP = 0 == iNumSimulation ? null : new
			org.drip.xva.trajectory.CollateralGroupPath[iNumSimulation];
		org.drip.xva.trajectory.CollateralGroupVertex[][] aaCGV = 0 == iNumSimulation || 1 >= iNumTimeStep ?
			null : new org.drip.xva.trajectory.CollateralGroupVertex[iNumSimulation][iNumTimeStep];

		if (iNumSimulation != aaCGVN.length || 1 >= iNumTimeStep || iNumTimeStep != adtVertex.length ||
			iNumTimeStep != aaCGVN[0].length || iNumTimeStep != aadblCollateralBalance[0].length)
			return null;

		try {
			for (int i = 0; i < iNumSimulation; ++i) {
				for (int j = 0; j < iNumTimeStep; ++j)
					aaCGV[i][j] = new org.drip.xva.trajectory.CollateralGroupVertex (adtVertex[j], new
						org.drip.xva.trajectory.CollateralGroupVertexExposure (aadblPortfolioValue[i][j], 0.,
							aadblCollateralBalance[i][j]), aaCGVN[i][j]);
			}

			for (int i = 0; i < iNumSimulation; ++i) {
				org.drip.xva.trajectory.CollateralGroupEdge[] aCGE = new
					org.drip.xva.trajectory.CollateralGroupEdge[iNumTimeStep - 1];

				for (int j = 1; j < iNumTimeStep; ++j)
					aCGE[j - 1] = new org.drip.xva.trajectory.CollateralGroupEdge (aaCGV[i][j - 1],
						aaCGV[i][j]);

				aCGP[i] = new org.drip.xva.trajectory.CollateralGroupPath (aCGE);
			}

			if (null == aCGP) return null;

			org.drip.xva.trajectory.NettingGroupPath[] aNGP = new
				org.drip.xva.trajectory.NettingGroupPath[iNumSimulation];

			for (int i = 0; i < iNumSimulation; ++i)
				aNGP[i] = org.drip.xva.trajectory.NettingGroupPath.Mono (aCGP[i]);

			return new NettingGroupPathAggregator (aNGP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a Standard NettingGroupPathAggregator Instance
	 * 
	 * @param adtVertex Array of the Evolution Vertex Dates
	 * @param aadblPortfolioValue Array of the Portfolio Path Value Realizations
	 * @param aadblCollateralBalance Array of Path Collateral Balances
	 * @param aCGVN Array of the GroupTrajectoryVertexNumeraire Realizations
	 * 
	 * @return The Standard NettingGroupPathAggregator Instance
	 */

	public static final NettingGroupPathAggregator Standard (
		final org.drip.analytics.date.JulianDate[] adtVertex,
		final double[][] aadblPortfolioValue,
		final double[][] aadblCollateralBalance,
		final org.drip.xva.trajectory.CollateralGroupVertexNumeraire[] aCGVN)
	{
		if (null == aadblPortfolioValue) return null;

		int iNumSimulation = aadblPortfolioValue.length;
		org.drip.xva.trajectory.CollateralGroupVertexNumeraire[][] aaCGVN = 0 == iNumSimulation ? null : new
			org.drip.xva.trajectory.CollateralGroupVertexNumeraire[iNumSimulation][];

		for (int i = 0; i < iNumSimulation; ++i)
			aaCGVN[i] = aCGVN;

		return Standard (adtVertex, aadblPortfolioValue, aadblCollateralBalance, aaCGVN);
	}

	/**
	 * Construct a Standard NettingGroupPathAggregator Instance
	 * 
	 * @param aCGP Array of the GroupTrajectoryPath Realizations
	 * 
	 * @return The Standard NettingGroupPathAggregator Instance
	 */

	public static final NettingGroupPathAggregator Standard (
		final org.drip.xva.trajectory.CollateralGroupPath[] aCGP)
	{
		if (null == aCGP) return null;

		int iNumNettingGroupPath = aCGP.length;
		org.drip.xva.trajectory.NettingGroupPath[] aNGP = 0 == iNumNettingGroupPath ? null : new
			org.drip.xva.trajectory.NettingGroupPath[iNumNettingGroupPath];

		if (0 == iNumNettingGroupPath) return null;

		for (int i = 0; i < iNumNettingGroupPath; ++i)
			aNGP[i] = org.drip.xva.trajectory.NettingGroupPath.Mono (aCGP[i]);

		try {
			return new NettingGroupPathAggregator (aNGP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * NettingGroupPathAggregator Constructor
	 * 
	 * @param aNGP The Array of Collateral Group Digests
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NettingGroupPathAggregator (
		final org.drip.xva.trajectory.NettingGroupPath[] aNGP)
		throws java.lang.Exception
	{
		if (null == (_aNGP = aNGP) || 0 == _aNGP.length)
			throw new java.lang.Exception ("NettingGroupPathAggregator Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Array of the Netting Group Trajectory Paths
	 * 
	 * @return The Array of the Netting Group Trajectory Paths
	 */

	public org.drip.xva.trajectory.NettingGroupPath[] nettingGroupTrajectoryPaths()
	{
		return _aNGP;
	}

	/**
	 * Retrieve the Array of the Vertex Dates
	 * 
	 * @return The Array of the Vertex Dates
	 */

	public org.drip.analytics.date.JulianDate[] vertexes()
	{
		return _aNGP[0].vertexes();
	}

	/**
	 * Retrieve the Expected CVA
	 * 
	 * @return The Expected CVA
	 */

	public double cva()
	{
		double dblCVASum = 0.;
		int iNumPath = _aNGP.length;

		for (int i = 0; i < iNumPath; ++i)
			dblCVASum += _aNGP[i].cva();

		return dblCVASum / iNumPath;
	}

	/**
	 * Retrieve the Expected DVA
	 * 
	 * @return The Expected DVA
	 */

	public double dva()
	{
		double dblDVASum = 0.;
		int iNumPath = _aNGP.length;

		for (int i = 0; i < iNumPath; ++i)
			dblDVASum += _aNGP[i].dva();

		return dblDVASum / iNumPath;
	}

	/**
	 * Retrieve the Expected FCA
	 * 
	 * @return The Expected FCA
	 */

	public double fca()
	{
		double dblFCASum = 0.;
		int iNumPath = _aNGP.length;

		for (int i = 0; i < iNumPath; ++i)
			dblFCASum += _aNGP[i].fca();

		return dblFCASum / iNumPath;
	}

	/**
	 * Retrieve the Expected Total VA
	 * 
	 * @return The Expected Total VA
	 */

	public double total()
	{
		double dblTotalSum = 0.;
		int iNumPath = _aNGP.length;

		for (int i = 0; i < iNumPath; ++i)
			dblTotalSum += _aNGP[i].total();

		return dblTotalSum / iNumPath;
	}

	/**
	 * Retrieve the Array of Collateralized Exposures
	 * 
	 * @return The Array of Collateralized Exposures
	 */

	public double[] collateralizedExposure()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblCollateralizedExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedExposure[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathCollateralizedExposure = _aNGP[iPathIndex].collateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblCollateralizedExposure[iEdgeIndex] += adblPathCollateralizedExposure[iEdgeIndex];
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedExposure[j] /= iNumPath;

		return adblCollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposures
	 * 
	 * @return The Array of Uncollateralized Exposures
	 */

	public double[] uncollateralizedExposure()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblUncollateralizedExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedExposure[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathUncollateralizedExposure = _aNGP[iPathIndex].uncollateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblUncollateralizedExposure[iEdgeIndex] += adblPathUncollateralizedExposure[iEdgeIndex];
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedExposure[j] /= iNumPath;

		return adblUncollateralizedExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Exposure PV's
	 * 
	 * @return The Array of Collateralized Exposure PV's
	 */

	public double[] collateralizedExposurePV()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblCollateralizedExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedExposurePV[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathCollateralizedExposurePV = _aNGP[iPathIndex].collateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblCollateralizedExposurePV[iEdgeIndex] += adblPathCollateralizedExposurePV[iEdgeIndex];
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedExposurePV[j] /= iNumPath;

		return adblCollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Exposure PV's
	 * 
	 * @return The Array of Uncollateralized Exposure PV's
	 */

	public double[] uncollateralizedExposurePV()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblUncollateralizedExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedExposurePV[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathUncollateralizedExposurePV = _aNGP[iPathIndex].uncollateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex)
				adblUncollateralizedExposurePV[iEdgeIndex] += adblPathUncollateralizedExposurePV[iEdgeIndex];
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedExposurePV[j] /= iNumPath;

		return adblUncollateralizedExposurePV;
	}

	/**
	 * Retrieve the Array of Collateralized Positive Exposures
	 * 
	 * @return The Array of Collateralized Positive Exposures
	 */

	public double[] collateralizedPositiveExposure()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblCollateralizedPositiveExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedPositiveExposure[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathCollateralizedExposure = _aNGP[iPathIndex].collateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeCollateralizedExposure = adblPathCollateralizedExposure[iEdgeIndex];

				if (0 < dblPathEdgeCollateralizedExposure)
					adblCollateralizedPositiveExposure[iEdgeIndex] += dblPathEdgeCollateralizedExposure;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedPositiveExposure[j] /= iNumPath;

		return adblCollateralizedPositiveExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Positive Exposure PV
	 * 
	 * @return The Array of Collateralized Positive Exposure PV
	 */

	public double[] collateralizedPositiveExposurePV()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblCollateralizedPositiveExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedPositiveExposurePV[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathCollateralizedExposurePV = _aNGP[iPathIndex].collateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeCollateralizedExposurePV = adblPathCollateralizedExposurePV[iEdgeIndex];

				if (0 < dblPathEdgeCollateralizedExposurePV)
					adblCollateralizedPositiveExposurePV[iEdgeIndex] += dblPathEdgeCollateralizedExposurePV;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedPositiveExposurePV[j] /= iNumPath;

		return adblCollateralizedPositiveExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Positive Exposures
	 * 
	 * @return The Array of Uncollateralized Positive Exposures
	 */

	public double[] uncollateralizedPositiveExposure()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblUncollateralizedPositiveExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedPositiveExposure[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathUncollateralizedExposure = _aNGP[iPathIndex].uncollateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeUncollateralizedExposure = adblPathUncollateralizedExposure[iEdgeIndex];

				if (0 < dblPathEdgeUncollateralizedExposure)
					adblUncollateralizedPositiveExposure[iEdgeIndex] += dblPathEdgeUncollateralizedExposure;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedPositiveExposure[j] /= iNumPath;

		return adblUncollateralizedPositiveExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Positive Exposure PV
	 * 
	 * @return The Array of Uncollateralized Positive Exposure PV
	 */

	public double[] uncollateralizedPositiveExposurePV()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblUncollateralizedPositiveExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedPositiveExposurePV[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathUncollateralizedExposurePV = _aNGP[iPathIndex].uncollateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeUncollateralizedExposurePV =
					adblPathUncollateralizedExposurePV[iEdgeIndex];

				if (0 < dblPathEdgeUncollateralizedExposurePV)
					adblUncollateralizedPositiveExposurePV[iEdgeIndex] +=
						dblPathEdgeUncollateralizedExposurePV;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedPositiveExposurePV[j] /= iNumPath;

		return adblUncollateralizedPositiveExposurePV;
	}

	/**
	 * Retrieve the Array of Collateralized Negative Exposures
	 * 
	 * @return The Array of Collateralized Negative Exposures
	 */

	public double[] collateralizedNegativeExposure()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblCollateralizedNegativeExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedNegativeExposure[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathCollateralizedExposure = _aNGP[iPathIndex].collateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeCollateralizedExposure = adblPathCollateralizedExposure[iEdgeIndex];

				if (0 > dblPathEdgeCollateralizedExposure)
					adblCollateralizedNegativeExposure[iEdgeIndex] += dblPathEdgeCollateralizedExposure;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedNegativeExposure[j] /= iNumPath;

		return adblCollateralizedNegativeExposure;
	}

	/**
	 * Retrieve the Array of Collateralized Negative Exposure PV
	 * 
	 * @return The Array of Collateralized Negative Exposure PV
	 */

	public double[] collateralizedNegativeExposurePV()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblCollateralizedNegativeExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedNegativeExposurePV[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathCollateralizedExposurePV = _aNGP[iPathIndex].collateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeCollateralizedExposurePV = adblPathCollateralizedExposurePV[iEdgeIndex];

				if (0 > dblPathEdgeCollateralizedExposurePV)
					adblCollateralizedNegativeExposurePV[iEdgeIndex] += dblPathEdgeCollateralizedExposurePV;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblCollateralizedNegativeExposurePV[j] /= iNumPath;

		return adblCollateralizedNegativeExposurePV;
	}

	/**
	 * Retrieve the Array of Uncollateralized Negative Exposures
	 * 
	 * @return The Array of Uncollateralized Negative Exposures
	 */

	public double[] uncollateralizedNegativeExposure()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblUncollateralizedNegativeExposure = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedNegativeExposure[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathUncollateralizedExposure = _aNGP[iPathIndex].uncollateralizedExposure();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeUncollateralizedExposure = adblPathUncollateralizedExposure[iEdgeIndex];

				if (0 > dblPathEdgeUncollateralizedExposure)
					adblUncollateralizedNegativeExposure[iEdgeIndex] += dblPathEdgeUncollateralizedExposure;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedNegativeExposure[j] /= iNumPath;

		return adblUncollateralizedNegativeExposure;
	}

	/**
	 * Retrieve the Array of Uncollateralized Negative Exposure PV
	 * 
	 * @return The Array of Uncollateralized Negative Exposure PV
	 */

	public double[] uncollateralizedNegativeExposurePV()
	{
		int iNumEdge = _aNGP[0].vertexes().length - 1;

		int iNumPath = _aNGP.length;
		double[] adblUncollateralizedNegativeExposurePV = new double[iNumEdge];

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedNegativeExposurePV[j] = 0.;

		for (int iPathIndex = 0; iPathIndex < iNumPath; ++iPathIndex) {
			double[] adblPathUncollateralizedExposurePV = _aNGP[iPathIndex].uncollateralizedExposurePV();

			for (int iEdgeIndex = 0; iEdgeIndex < iNumEdge; ++iEdgeIndex) {
				double dblPathEdgeUncollateralizedExposurePV =
					adblPathUncollateralizedExposurePV[iEdgeIndex];

				if (0 > dblPathEdgeUncollateralizedExposurePV)
					adblUncollateralizedNegativeExposurePV[iEdgeIndex] +=
						dblPathEdgeUncollateralizedExposurePV;
			}
		}

		for (int j = 0; j < iNumEdge; ++j)
			adblUncollateralizedNegativeExposurePV[j] /= iNumPath;

		return adblUncollateralizedNegativeExposurePV;
	}
}
