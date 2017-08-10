
package org.drip.sample.netting;

import org.drip.analytics.date.*;
import org.drip.measure.discretemarginal.SequenceGenerator;
import org.drip.measure.dynamics.DiffusionEvaluatorLogarithmic;
import org.drip.measure.process.DiffusionEvolver;
import org.drip.measure.realization.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.env.EnvManager;
import org.drip.xva.trajectory.*;

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
 * PortfolioGroupRun demonstrates the Simulation Run of the Netting Group Exposure. The References are:
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

public class PortfolioGroupRun {

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		EnvManager.InitEnv ("");

		int iNumStep = 10;
		double dblTime = 5.;
		double dblAssetDrift = 0.06;
		double dblAssetVolatility = 0.15;
		double dblInitialAssetValue = 1.;
		double dblCollateralDrift = 0.01;
		double dblBankHazardRate = 0.015;
		double dblBankRecoveryRate = 0.40;
		double dblCounterPartyHazardRate = 0.030;
		double dblCounterPartyRecoveryRate = 0.30;

		double dblTimeWidth = dblTime / iNumStep;
		double[] adblCollateral = new double[iNumStep];
		double[] adblBankSurvival = new double[iNumStep];
		double[] adblBankRecovery = new double[iNumStep];
		JulianDate[] adtVertex = new JulianDate[iNumStep];
		double[] adblBankFundingSpread = new double[iNumStep];
		double[] adblCounterPartySurvival = new double[iNumStep];
		double[] adblCounterPartyRecovery = new double[iNumStep];
		CollateralGroupEdge[] aCGE1 = new CollateralGroupEdge[iNumStep - 1];
		CollateralGroupEdge[] aCGE2 = new CollateralGroupEdge[iNumStep - 1];
		CollateralGroupVertex[] aCGV1 = new CollateralGroupVertex[iNumStep];
		CollateralGroupVertex[] aCGV2 = new CollateralGroupVertex[iNumStep];
		double dblBankFundingSpread = dblBankHazardRate / (1. - dblBankRecoveryRate);
		CollateralGroupVertexExposure[] aCGVE1 = new CollateralGroupVertexExposure[iNumStep];
		CollateralGroupVertexExposure[] aCGVE2 = new CollateralGroupVertexExposure[iNumStep];
		CollateralGroupVertexNumeraire[] aCGVN = new CollateralGroupVertexNumeraire[iNumStep];

		JulianDate dtSpot = DateUtil.Today();

		DiffusionEvolver deAsset = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblAssetDrift,
				dblAssetVolatility
			)
		);

		JumpDiffusionEdge[] aJDEAsset1 = deAsset.incrementSequence (
			new JumpDiffusionVertex (
				dblTime,
				dblInitialAssetValue,
				0.,
				false
			),
			UnitRandom.Diffusion (SequenceGenerator.Gaussian (iNumStep)),
			dblTimeWidth
		);

		JumpDiffusionEdge[] aJDEAsset2 = deAsset.incrementSequence (
			new JumpDiffusionVertex (
				dblTime,
				dblInitialAssetValue,
				0.,
				false
			),
			UnitRandom.Diffusion (SequenceGenerator.Gaussian (iNumStep)),
			dblTimeWidth
		);

		System.out.println();

		System.out.println ("\t|--------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|                                                       PATH VERTEX EXPOSURES AND NUMERAIRE REALIZATIONS                                                       ||");

		System.out.println ("\t|--------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|    L -> R:                                                                                                                                                   ||");

		System.out.println ("\t|            - Path #1 Gross Exposure                                                                                                                          ||");

		System.out.println ("\t|            - Path #1 Positive Exposure                                                                                                                       ||");

		System.out.println ("\t|            - Path #1 Negative Exposure                                                                                                                       ||");

		System.out.println ("\t|            - Path #2 Gross Exposure                                                                                                                          ||");

		System.out.println ("\t|            - Path #2 Positive Exposure                                                                                                                       ||");

		System.out.println ("\t|            - Path #2 Negative Exposure                                                                                                                       ||");

		System.out.println ("\t|            - Collateral Numeraire                                                                                                                            ||");

		System.out.println ("\t|            - Bank Survival Probability                                                                                                                       ||");

		System.out.println ("\t|            - Bank Recovery Rate                                                                                                                              ||");

		System.out.println ("\t|            - Bank Funding Spread                                                                                                                             ||");

		System.out.println ("\t|            - Counter Party Survival Probability                                                                                                              ||");

		System.out.println ("\t|            - Counter Party Recovery Rate                                                                                                                     ||");

		System.out.println ("\t|--------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		for (int i = 0; i < aJDEAsset1.length; ++i) {
			adblBankRecovery[i] = dblBankRecoveryRate;
			adblBankFundingSpread[i] = dblBankFundingSpread;
			adblCounterPartyRecovery[i] = dblCounterPartyRecoveryRate;

			adtVertex[i] = dtSpot.addMonths (6 * i + 6);

			adblCollateral[i] = Math.exp (0.5 * dblCollateralDrift * (i + 1));

			adblBankSurvival[i] = Math.exp (-0.5 * dblBankHazardRate * (i + 1));

			aCGVE1[i] = new CollateralGroupVertexExposure (
				aJDEAsset1[i].finish(),
				0.,
				0.
			);

			aCGVE2[i] = new CollateralGroupVertexExposure (
				aJDEAsset2[i].finish(),
				0.,
				0.
			);

			adblCounterPartySurvival[i] = Math.exp (-0.5 * dblCounterPartyHazardRate * (i + 1));

			aCGVN[i] = new CollateralGroupVertexNumeraire (
				adblCollateral[i],
				adblBankSurvival[i],
				adblBankRecovery[i],
				adblBankFundingSpread[i],
				adblCounterPartySurvival[i],
				adblCounterPartyRecovery[i]
			);

			aCGV1[i] = new CollateralGroupVertex (
				adtVertex[i],
				aCGVE1[i],
				aCGVN[i]
			);

			aCGV2[i] = new CollateralGroupVertex (
				adtVertex[i],
				aCGVE2[i],
				aCGVN[i]
			);

			System.out.println (
				"\t| " + adtVertex[i] + " => " +
				FormatUtil.FormatDouble (aCGVE1[i].net(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGVE1[i].positive(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGVE1[i].negative(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGVE2[i].net(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGVE2[i].positive(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGVE2[i].negative(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (adblCollateral[i], 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (adblBankSurvival[i], 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (adblBankRecovery[i], 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (adblBankFundingSpread[i], 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (adblCounterPartySurvival[i], 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (adblCounterPartyRecovery[i], 1, 6, 1.) + " ||"
			);
		}

		System.out.println ("\t|--------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println();

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|                          PERIOD CREDIT, DEBT, AND FUNDING VALUATION ADJUSTMENTS                          ||");

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|    - Forward Period                                                                                      ||");

		System.out.println ("\t|    - Path #1 Period Credit Adjustments                                                                   ||");

		System.out.println ("\t|    - Path #1 Period Debt Adjustments                                                                     ||");

		System.out.println ("\t|    - Path #1 Period Funding Adjustments                                                                  ||");

		System.out.println ("\t|    - Path #2 Period Credit Adjustments                                                                   ||");

		System.out.println ("\t|    - Path #2 Period Debt Adjustments                                                                     ||");

		System.out.println ("\t|    - Path #2 Period Funding Adjustments                                                                  ||");

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------||");

		for (int i = 1; i < aJDEAsset1.length; ++i) {
			aCGE1[i - 1] = new CollateralGroupEdge (
				aCGV1[i - 1],
				aCGV1[i]
			);

			aCGE2[i - 1] = new CollateralGroupEdge (
				aCGV2[i - 1],
				aCGV2[i]
			);

			System.out.println ("\t| [" +
				aCGE1[i - 1].head().vertex() + " -> " + aCGE1[i - 1].tail().vertex() + "] => " +
				FormatUtil.FormatDouble (aCGE1[i - 1].credit(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGE1[i - 1].debt(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGE1[i - 1].funding(), 1, 6, 1.) + " || " +
				FormatUtil.FormatDouble (aCGE2[i - 1].credit(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGE2[i - 1].debt(), 1, 6, 1.) + " | " +
				FormatUtil.FormatDouble (aCGE2[i - 1].funding(), 1, 6, 1.) + " ||"
			);
		}

		System.out.println ("\t|----------------------------------------------------------------------------------------------------------||");

		System.out.println();

		NettingGroupPathAggregator ngpa = NettingGroupPathAggregator.Standard (
			new CollateralGroupPath[] {
				new CollateralGroupPath (aCGE1),
				new CollateralGroupPath (aCGE2)
			}
		);

		JulianDate[] adtVertexNode = ngpa.vertexes();

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		String strDump = "\t|         DATE         =>" ;

		for (int i = 0; i < adtVertexNode.length; ++i)
			strDump = strDump + " " + adtVertexNode[i] + " |";

		System.out.println (strDump);

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		double[] adblEE = ngpa.collateralizedExposure();

		strDump = "\t|       EXPOSURE       =>   " + FormatUtil.FormatDouble (dblInitialAssetValue, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPE = ngpa.collateralizedPositiveExposure();

		strDump = "\t|  POSITIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (dblInitialAssetValue, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEPE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEPE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblENE = ngpa.collateralizedNegativeExposure();

		strDump = "\t|  NEGATIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (0., 1, 4, 1.) + "   |";

		for (int j = 0; j < adblENE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblENE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEEPV = ngpa.collateralizedExposurePV();

		strDump = "\t|      EXPOSURE PV     =>   " + FormatUtil.FormatDouble (dblInitialAssetValue, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPEPV = ngpa.collateralizedPositiveExposurePV();

		strDump = "\t| POSITIVE EXPOSURE PV =>   " + FormatUtil.FormatDouble (dblInitialAssetValue, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEPEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEPEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblENEPV = ngpa.collateralizedNegativeExposurePV();

		strDump = "\t| NEGATIVE EXPOSURE PV =>   " + FormatUtil.FormatDouble (0., 1, 4, 1.) + "   |";

		for (int j = 0; j < adblENEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblENEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println();

		System.out.println ("\t||----------------||");

		System.out.println ("\t|| CVA => " + FormatUtil.FormatDouble (ngpa.cva(), 2, 2, 100.) + "% ||");

		System.out.println ("\t|| DVA => " + FormatUtil.FormatDouble (ngpa.dva(), 2, 2, 100.) + "% ||");

		System.out.println ("\t|| FVA => " + FormatUtil.FormatDouble (ngpa.fca(), 2, 2, 100.) + "% ||");

		System.out.println ("\t||----------------||");
	}
}
