package org.mtransit.parser.ca_calgary_transit_bus;

import static org.mtransit.parser.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MTrip;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://www.calgarytransit.com/developer-resources
// https://data.calgary.ca/
// https://data.calgary.ca/en/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj
// https://data.calgary.ca/download/npk7-z3bj/application%2Fzip
// https://data.calgary.ca/download/npk7-z3bj/application%2Fx-zip-compressed
// https://data.calgary.ca/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj
// https://data.calgary.ca/d/npk7-z3bj?category=Transportation-Transit&view_name=Calgary-Transit-Scheduling-Data
// https://data.calgary.ca/api/file_data/38ff3c2d-efde-4d50-b83c-3a2f49f390e5?filename=CT_GTFS.zip
public class CalgaryTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CalgaryTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Calgary Transit";
	}

	private static final Pattern OUT_OF_SERVICE = Pattern.compile("((^|\\W)(out of service)(\\W|$))", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (OUT_OF_SERVICE.matcher(gTrip.getTripHeadsignOrDefault()).find()) {
			return true; // exclude
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "FLT":
			return 10_001L;
		default:
			return super.convertRouteIdFromShortNameNotSupported(routeShortName);
		}
	}

	private static final Pattern CLEAN_STREET_POINT = Pattern.compile("((\\s)*(ave|st|mt)\\.(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_AVE_POINT_REPLACEMENT = "$2$3$4";

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CLEAN_STREET_POINT.matcher(routeLongName).replaceAll(CLEAN_AVE_POINT_REPLACEMENT);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_RED = "B83A3F"; // LIGHT RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	// private static final String COLOR_BUS_ROUTES = "004B85"; // BLUE (from PDF map)
	private static final String COLOR_BUS_ROUTES_EXPRESS = "00BBE5"; // LIGHT BLUE (from PDF map)
	private static final String COLOR_BUS_ROUTES_BRT = "ED1C2E"; // RED (from PDF map)
	private static final String COLOR_BUS_ROUTES_SCHOOL = "E4A024"; // YELLOW (from PDF map)

	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final String rsnS = gRoute.getRouteShortName();
		if (!CharUtils.isDigitsOnly(rsnS)) {
			if ("FLT".equals(rsnS)) {
				return null;
			}
		}
		final int rsn = Integer.parseInt(rsnS);
		if (rsn >= 600 && rsn <= 899) {
			return COLOR_BUS_ROUTES_SCHOOL;
		}
		final String rln = gRoute.getRouteLongNameOrDefault();
		if (ENDS_WITH_EXPRESS.matcher(rln).find()) {
			return COLOR_BUS_ROUTES_EXPRESS;
		}
		if (STARTS_WITH_BRT.matcher(rln).find()) {
			return COLOR_BUS_ROUTES_BRT;
		}
		if (rsn >= 1 && rsn <= 299) {
			return null;
		}
		if (rsn == 303) { // MO MAX Orange
			return "EF8B22";
		} else if (rsn == 304) { // MY MAX Yellow
			return "FFCD02";
		} else if (rsn == 306) { // MT MAX Teal
			return "009bA7";
		} else if (rsn == 307) { // MP MAX Purple
			return "92368D";
		}
		if (rsn >= 400 && rsn <= 599) {
			return null;
		}
		throw new MTLog.Fatal("Unexpected route color %s!", gRoute);
	}

	@Override
	public boolean directionSplitterEnabled() {
		return true; // ONLY FOR ROUTE 30
	}

	@Override
	public boolean directionOverrideId(long routeId) {
		if (routeId == 30L) {
			return true; // because loop + branch w/ same last stop ID
		}
		return super.directionOverrideId(routeId);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public List<Integer> getDirectionTypes() {
		return Arrays.asList(
				MTrip.HEADSIGN_TYPE_DIRECTION,
				MTrip.HEADSIGN_TYPE_STRING
		);
	}

	@Nullable
	@Override
	public MDirectionType convertDirection(@Nullable String headSign) {
		if (headSign != null) {
			final String headSignLC = headSign.toLowerCase(Locale.ENGLISH);
			if (headSignLC.endsWith(" - north")) {
				return MDirectionType.NORTH;
			} else if (headSignLC.endsWith(" - south")) {
				return MDirectionType.SOUTH;
			}
		}
		return null;
	}

	private static final Pattern ENDS_PARENTHESES = Pattern.compile("( \\([^(]+\\)$)");

	private static final Pattern STARTS_WITH_BOUNDS = Pattern.compile("(^" + "([A-Z]{2})?" + " )");
	private static final Pattern ENDS_WITH_BOUNDS = Pattern.compile("( " + "([A-Z]{2})?" + "$)");

	private static final Pattern BOUNDS_BEFORE_AT = Pattern.compile("( " + "([A-Z]{2})?( @)" + " )");
	private static final String BOUNDS_BEFORE_AT_REPLACEMENT = "$3 ";

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = super.cleanDirectionHeadsign(fromStopName, directionHeadSign);
		directionHeadSign = ENDS_PARENTHESES.matcher(directionHeadSign).replaceAll(EMPTY);
		directionHeadSign = STARTS_WITH_BOUNDS.matcher(directionHeadSign).replaceAll(EMPTY);
		directionHeadSign = ENDS_WITH_BOUNDS.matcher(directionHeadSign).replaceAll(EMPTY);
		directionHeadSign = BOUNDS_BEFORE_AT.matcher(directionHeadSign).replaceAll(BOUNDS_BEFORE_AT_REPLACEMENT);
		return directionHeadSign;
	}

	private static final Pattern AVENUE_ = Pattern.compile("((^|\\W)(av)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String AVENUE_REPLACEMENT = "$2" + "Avenue" + "$4";

	private static final String MRU = "MRU";
	private static final Pattern MRU_ = Pattern.compile("((^|\\W)(mru|mount royal university)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String MRU_REPLACEMENT = "$2" + MRU + "$4";

	private static final Pattern STN = Pattern.compile("((^|\\W)(stn)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String STN_REPLACEMENT = "$2" + "Station" + "$4";

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("((\\W)(express)($))", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BRT = Pattern.compile("((^)(brt)(\\W))", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_MAX_NAME_ = Pattern.compile("((^)(max [\\w]+)(\\W))", Pattern.CASE_INSENSITIVE);

	private static final Pattern ROUTE_RSN = Pattern.compile("((^)(route )?([\\d]+)($))", Pattern.CASE_INSENSITIVE);

	private static final Pattern CLEAN_AT_SPACE = Pattern.compile("(\\w)[\\s]*[@][\\s]*(\\w)");
	private static final String CLEAN_AT_SPACE_REPLACEMENT = "$1 @ $2";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CLEAN_AT_SPACE.matcher(tripHeadsign).replaceAll(CLEAN_AT_SPACE_REPLACEMENT);
		tripHeadsign = AVENUE_.matcher(tripHeadsign).replaceAll(AVENUE_REPLACEMENT);
		tripHeadsign = STARTS_WITH_BRT.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STARTS_WITH_MAX_NAME_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = MRU_.matcher(tripHeadsign).replaceAll(MRU_REPLACEMENT);
		tripHeadsign = CleanUtils.fixMcXCase(tripHeadsign);
		tripHeadsign = STN.matcher(tripHeadsign).replaceAll(STN_REPLACEMENT);
		tripHeadsign = ROUTE_RSN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"AM", "PM",
				"EB", "WB", "NB", "SB",
				"SE", "SW", "NE", "NW",
				"LRT", "YYC", "TRW", "MRU", "SAIT", "JG", "EEEL",
				"CTrain",
				"CT",
				"SC"
		};
	}

	private static final Pattern STARTS_WITH_SLASH = Pattern.compile("(^[\\s]*/[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final String REGEX_START_END = "((^|[^A-Z]){1}(%s)([^a-zA-Z]|$){1})";
	private static final String REGEX_START_END_REPLACEMENT = "$2 %s $4";

	private static final Pattern AV = Pattern.compile(String.format(REGEX_START_END, "AV|AVE"));
	private static final String AV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Avenue");

	private static final Pattern PA = Pattern.compile(String.format(REGEX_START_END, "PA"));
	private static final String PA_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Park");

	private static final Pattern HT = Pattern.compile(String.format(REGEX_START_END, "HT"));
	private static final String HT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Heights");

	private static final Pattern GV = Pattern.compile(String.format(REGEX_START_END, "GV"));
	private static final String GV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Grove");

	private static final Pattern PT = Pattern.compile(String.format(REGEX_START_END, "PT"));
	private static final String PT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Point");

	private static final Pattern TC = Pattern.compile(String.format(REGEX_START_END, "TC"));
	private static final String TC_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Terrace");

	private static final Pattern RI = Pattern.compile(String.format(REGEX_START_END, "RI"));
	private static final String RI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Rise");

	private static final Pattern MR = Pattern.compile(String.format(REGEX_START_END, "MR"));
	private static final String MR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Manor");

	private static final Pattern DR = Pattern.compile(String.format(REGEX_START_END, "DR"));
	private static final String DR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Drive");

	private static final Pattern ST = Pattern.compile(String.format(REGEX_START_END, "ST"));
	private static final String ST_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Street");

	private static final Pattern VI = Pattern.compile(String.format(REGEX_START_END, "VI"));
	private static final String VI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Villas");

	private static final Pattern PZ = Pattern.compile(String.format(REGEX_START_END, "PZ"));
	private static final String PZ_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Plaza");

	private static final Pattern WY = Pattern.compile(String.format(REGEX_START_END, "WY"));
	private static final String WY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Way");

	private static final Pattern GR = Pattern.compile(String.format(REGEX_START_END, "GR"));
	private static final String GR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Green");

	private static final Pattern BV = Pattern.compile(String.format(REGEX_START_END, "BV"));
	private static final String BV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Boulevard");

	private static final Pattern GA = Pattern.compile(String.format(REGEX_START_END, "GA"));
	private static final String GA_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Gate");

	private static final Pattern RD = Pattern.compile(String.format(REGEX_START_END, "RD"));
	private static final String RD_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Road");

	private static final Pattern LI = Pattern.compile(String.format(REGEX_START_END, "LI|LINK"));
	private static final String LI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Link");

	private static final Pattern PL = Pattern.compile(String.format(REGEX_START_END, "PL"));
	private static final String PL_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Place");

	private static final Pattern SQ = Pattern.compile(String.format(REGEX_START_END, "SQ"));
	private static final String SQ_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Square");

	private static final Pattern CL = Pattern.compile(String.format(REGEX_START_END, "CL"));
	private static final String CL_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Close");

	private static final Pattern CR = Pattern.compile(String.format(REGEX_START_END, "CR"));
	private static final String CR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Crescent");

	private static final Pattern GD = Pattern.compile(String.format(REGEX_START_END, "GD"));
	private static final String GD_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Gardens");

	private static final Pattern LN = Pattern.compile(String.format(REGEX_START_END, "LN"));
	private static final String LN_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Lane");

	private static final Pattern CO = Pattern.compile(String.format(REGEX_START_END, "CO"));
	private static final String CO_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Ct");

	private static final Pattern CI = Pattern.compile(String.format(REGEX_START_END, "CI"));
	private static final String CI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Circle");

	private static final Pattern HE = Pattern.compile(String.format(REGEX_START_END, "HE"));
	private static final String HE_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Heath");

	private static final Pattern ME = Pattern.compile(String.format(REGEX_START_END, "ME"));
	private static final String ME_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Mews");

	private static final Pattern TR = Pattern.compile(String.format(REGEX_START_END, "TR"));
	private static final String TR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Trail");

	private static final Pattern LD = Pattern.compile(String.format(REGEX_START_END, "LD"));
	private static final String LD_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Landing");

	private static final Pattern HL = Pattern.compile(String.format(REGEX_START_END, "HL"));
	private static final String HL_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Hill");

	private static final Pattern PK = Pattern.compile(String.format(REGEX_START_END, "PK"));
	private static final String PK_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Park");

	private static final Pattern CM = Pattern.compile(String.format(REGEX_START_END, "CM"));
	private static final String CM_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Common");

	private static final Pattern GT = Pattern.compile(String.format(REGEX_START_END, "GT"));
	private static final String GT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Gate");

	private static final Pattern CV = Pattern.compile(String.format(REGEX_START_END, "CV"));
	private static final String CV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Cove");

	private static final Pattern VW = Pattern.compile(String.format(REGEX_START_END, "VW"));
	private static final String VW_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "View");

	private static final Pattern BY = Pattern.compile(String.format(REGEX_START_END, "BY|BA|BAY"));
	private static final String BY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Bay");

	private static final Pattern CE = Pattern.compile(String.format(REGEX_START_END, "CE"));
	private static final String CE_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Center");

	private static final Pattern CTR = Pattern.compile(String.format(REGEX_START_END, "CTR"));
	private static final String CTR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Center");

	private static final Pattern PY_ = Pattern.compile(String.format(REGEX_START_END, "PY"));
	private static final String PY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Parkway");

	private static final Pattern PR_ = Pattern.compile(String.format(REGEX_START_END, "PR"));
	private static final String PR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Parade");

	private static final Pattern PS_ = Pattern.compile(String.format(REGEX_START_END, "PS"));
	private static final String PS_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Passage");

	private static final Pattern RO_ = Pattern.compile(String.format(REGEX_START_END, "RO"));
	private static final String RO_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Row");

	private static final Pattern MT_ = Pattern.compile(String.format(REGEX_START_END, "MT"));
	private static final String MT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Mount");

	private static final Pattern GDN_ = Pattern.compile(String.format(REGEX_START_END, "GDN"));
	private static final String GDN_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Garden");

	private static final Pattern TERR_ = Pattern.compile(String.format(REGEX_START_END, "TERR"));
	private static final String TERR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Terrace");

	private static final Pattern MOUNT_ROYAL_UNIVERSITY = Pattern.compile(String.format(REGEX_START_END, "Mount Royal University"));
	private static final String MOUNT_ROYAL_UNIVERSITY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "MRU");

	private static final Pattern MOUNT = Pattern.compile(String.format(REGEX_START_END, "Mount"));
	private static final String MOUNT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Mt");

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = AV.matcher(gStopName).replaceAll(AV_REPLACEMENT);
		gStopName = PA.matcher(gStopName).replaceAll(PA_REPLACEMENT);
		gStopName = HT.matcher(gStopName).replaceAll(HT_REPLACEMENT);
		gStopName = GV.matcher(gStopName).replaceAll(GV_REPLACEMENT);
		gStopName = PT.matcher(gStopName).replaceAll(PT_REPLACEMENT);
		gStopName = TC.matcher(gStopName).replaceAll(TC_REPLACEMENT);
		gStopName = RI.matcher(gStopName).replaceAll(RI_REPLACEMENT);
		gStopName = MR.matcher(gStopName).replaceAll(MR_REPLACEMENT);
		gStopName = DR.matcher(gStopName).replaceAll(DR_REPLACEMENT);
		gStopName = ST.matcher(gStopName).replaceAll(ST_REPLACEMENT);
		gStopName = VI.matcher(gStopName).replaceAll(VI_REPLACEMENT);
		gStopName = PZ.matcher(gStopName).replaceAll(PZ_REPLACEMENT);
		gStopName = WY.matcher(gStopName).replaceAll(WY_REPLACEMENT);
		gStopName = GR.matcher(gStopName).replaceAll(GR_REPLACEMENT);
		gStopName = BV.matcher(gStopName).replaceAll(BV_REPLACEMENT);
		gStopName = GA.matcher(gStopName).replaceAll(GA_REPLACEMENT);
		gStopName = RD.matcher(gStopName).replaceAll(RD_REPLACEMENT);
		gStopName = LI.matcher(gStopName).replaceAll(LI_REPLACEMENT);
		gStopName = PL.matcher(gStopName).replaceAll(PL_REPLACEMENT);
		gStopName = SQ.matcher(gStopName).replaceAll(SQ_REPLACEMENT);
		gStopName = CL.matcher(gStopName).replaceAll(CL_REPLACEMENT);
		gStopName = CR.matcher(gStopName).replaceAll(CR_REPLACEMENT);
		gStopName = GD.matcher(gStopName).replaceAll(GD_REPLACEMENT);
		gStopName = LN.matcher(gStopName).replaceAll(LN_REPLACEMENT);
		gStopName = CO.matcher(gStopName).replaceAll(CO_REPLACEMENT);
		gStopName = ME.matcher(gStopName).replaceAll(ME_REPLACEMENT);
		gStopName = TR.matcher(gStopName).replaceAll(TR_REPLACEMENT);
		gStopName = CI.matcher(gStopName).replaceAll(CI_REPLACEMENT);
		gStopName = HE.matcher(gStopName).replaceAll(HE_REPLACEMENT);
		gStopName = LD.matcher(gStopName).replaceAll(LD_REPLACEMENT);
		gStopName = HL.matcher(gStopName).replaceAll(HL_REPLACEMENT);
		gStopName = PK.matcher(gStopName).replaceAll(PK_REPLACEMENT);
		gStopName = CM.matcher(gStopName).replaceAll(CM_REPLACEMENT);
		gStopName = GT.matcher(gStopName).replaceAll(GT_REPLACEMENT);
		gStopName = CV.matcher(gStopName).replaceAll(CV_REPLACEMENT);
		gStopName = VW.matcher(gStopName).replaceAll(VW_REPLACEMENT);
		gStopName = BY.matcher(gStopName).replaceAll(BY_REPLACEMENT);
		gStopName = CE.matcher(gStopName).replaceAll(CE_REPLACEMENT);
		gStopName = CTR.matcher(gStopName).replaceAll(CTR_REPLACEMENT);
		gStopName = PY_.matcher(gStopName).replaceAll(PY_REPLACEMENT);
		gStopName = PR_.matcher(gStopName).replaceAll(PR_REPLACEMENT);
		gStopName = PS_.matcher(gStopName).replaceAll(PS_REPLACEMENT);
		gStopName = RO_.matcher(gStopName).replaceAll(RO_REPLACEMENT);
		gStopName = MT_.matcher(gStopName).replaceAll(MT_REPLACEMENT);
		gStopName = GDN_.matcher(gStopName).replaceAll(GDN_REPLACEMENT);
		gStopName = TERR_.matcher(gStopName).replaceAll(TERR_REPLACEMENT);
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CLEAN_AT_SPACE.matcher(gStopName).replaceAll(CLEAN_AT_SPACE_REPLACEMENT);
		gStopName = MOUNT_ROYAL_UNIVERSITY.matcher(gStopName).replaceAll(MOUNT_ROYAL_UNIVERSITY_REPLACEMENT);
		gStopName = MOUNT.matcher(gStopName).replaceAll(MOUNT_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = STARTS_WITH_SLASH.matcher(gStopName).replaceAll(EMPTY);
		return CleanUtils.cleanLabel(gStopName);
	}
}
