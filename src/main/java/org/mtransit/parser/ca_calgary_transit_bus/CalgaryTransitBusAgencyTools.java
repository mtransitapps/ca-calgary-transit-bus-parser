package org.mtransit.parser.ca_calgary_transit_bus;

import static org.mtransit.parser.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.Cleaner;
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

// https://data.calgary.ca/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj/about_data
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

	private static final Cleaner OUT_OF_SERVICE = new Cleaner("((^|\\W)(out of service)(\\W|$))", EMPTY, true);

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (OUT_OF_SERVICE.find(gTrip.getTripHeadsignOrDefault())) {
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
		return false; // used GTFS-RT
	}

	@Nullable
	@Override
	public String getRouteIdCleanupRegex() {
		return "\\-\\d+$";
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

	private static final Cleaner CLEAN_STREET_POINT = new Cleaner(
			"((\\s)*(ave|st|mt)\\.(\\s)*)",
			"$2$3$4",
			true);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CLEAN_STREET_POINT.clean(routeLongName);
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
		if (ENDS_WITH_EXPRESS.find(rln)) {
			return COLOR_BUS_ROUTES_EXPRESS;
		}
		if (STARTS_WITH_BRT.find(rln)) {
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
	public boolean directionSplitterEnabled(long routeId) {
		if (routeId == 30L) {
			return true; // 2022-06-25: because loop + branch w/ same last stop ID
		}
		return super.directionSplitterEnabled(routeId);
	}

	@Override
	public boolean directionOverrideId(long routeId) {
		if (routeId == 30L) {
			return true; // 2022-06-25: because loop + branch w/ same last stop ID
		}
		return super.directionOverrideId(routeId);
	}

	@Override
	public boolean allowNonDescriptiveHeadSigns(long routeId) {
		if (Arrays.asList(
				51L, // 2023-08-29: because same head-sign, last stop...
				164L // 2023-08-29: because same head-sign, last stop...
		).contains(routeId)) {
			return true;
		}
		return super.allowNonDescriptiveHeadSigns(routeId);
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

	private static final Cleaner ENDS_PARENTHESES = new Cleaner("( \\([^(]+\\)$)");

	private static final Cleaner STARTS_WITH_BOUNDS = new Cleaner("(^" + "([A-Z]{2})?" + " )");
	private static final Cleaner ENDS_WITH_BOUNDS = new Cleaner("( " + "([A-Z]{2})?" + "$)");

	private static final Cleaner BOUNDS_BEFORE_AT = new Cleaner("( " + "([A-Z]{2})?( @)" + " )", "$3 ");

	@NotNull
	@Override
	public String cleanDirectionHeadsign(int directionId, boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = super.cleanDirectionHeadsign(directionId, fromStopName, directionHeadSign);
		directionHeadSign = ENDS_PARENTHESES.clean(directionHeadSign);
		directionHeadSign = STARTS_WITH_BOUNDS.clean(directionHeadSign);
		directionHeadSign = ENDS_WITH_BOUNDS.clean(directionHeadSign);
		directionHeadSign = BOUNDS_BEFORE_AT.clean(directionHeadSign);
		return directionHeadSign;
	}

	private static final Cleaner AVENUE_ = new Cleaner("((^|\\W)(av)(\\W|$))", "$2" + "Avenue" + "$4", true);

	private static final String MRU = "MRU";
	private static final Cleaner MRU_ = new Cleaner("((^|\\W)(mru|mount royal university)(\\W|$))", "$2" + MRU + "$4", true);

	private static final Cleaner STN = new Cleaner("((^|\\W)(stn)(\\W|$))", "$2" + "Station" + "$4", true);

	private static final Cleaner ENDS_WITH_EXPRESS = new Cleaner("((\\W)(express)($))", EMPTY, true);

	private static final Cleaner STARTS_WITH_BRT = new Cleaner("((^)(brt)(\\W))", EMPTY, true);

	private static final Cleaner STARTS_WITH_MAX_NAME_ = new Cleaner("((^)(max \\w+)(\\W))", EMPTY, true);

	private static final Cleaner ROUTE_RSN = new Cleaner("((^)(route )?(\\d+)($))", EMPTY, true);

	private static final Cleaner CLEAN_AT_SPACE = new Cleaner("(\\w)\\s*@\\s*(\\w)", "$1 @ $2");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CLEAN_AT_SPACE.clean(tripHeadsign);
		tripHeadsign = AVENUE_.clean(tripHeadsign);
		tripHeadsign = STARTS_WITH_BRT.clean(tripHeadsign);
		tripHeadsign = STARTS_WITH_MAX_NAME_.clean(tripHeadsign);
		tripHeadsign = MRU_.clean(tripHeadsign);
		tripHeadsign = CleanUtils.fixMcXCase(tripHeadsign);
		tripHeadsign = STN.clean(tripHeadsign);
		tripHeadsign = ROUTE_RSN.clean(tripHeadsign);
		tripHeadsign = ENDS_WITH_EXPRESS.clean(tripHeadsign);
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

	private static final Cleaner STARTS_WITH_SLASH = new Cleaner("(^\\s*/\\s*)", EMPTY, true);

	private static final String REGEX_START_END = "((^|[^A-Z]){1}(%s)([^a-zA-Z]|$){1})";
	private static final String REGEX_START_END_REPLACEMENT = "$2 %s $4";

	private static final Cleaner AV = new Cleaner(String.format(REGEX_START_END, "AV|AVE"), String.format(REGEX_START_END_REPLACEMENT, "Avenue"));

	private static final Cleaner PA = new Cleaner(String.format(REGEX_START_END, "PA"), String.format(REGEX_START_END_REPLACEMENT, "Park"));

	private static final Cleaner HT = new Cleaner(String.format(REGEX_START_END, "HT"), String.format(REGEX_START_END_REPLACEMENT, "Heights"));

	private static final Cleaner GV = new Cleaner(String.format(REGEX_START_END, "GV"), String.format(REGEX_START_END_REPLACEMENT, "Grove"));

	private static final Cleaner PT = new Cleaner(String.format(REGEX_START_END, "PT"), String.format(REGEX_START_END_REPLACEMENT, "Point"));

	private static final Cleaner TC = new Cleaner(String.format(REGEX_START_END, "TC"), String.format(REGEX_START_END_REPLACEMENT, "Terrace"));

	private static final Cleaner RI = new Cleaner(String.format(REGEX_START_END, "RI"), String.format(REGEX_START_END_REPLACEMENT, "Rise"));

	private static final Cleaner MR = new Cleaner(String.format(REGEX_START_END, "MR"), String.format(REGEX_START_END_REPLACEMENT, "Manor"));

	private static final Cleaner DR = new Cleaner(String.format(REGEX_START_END, "DR"), String.format(REGEX_START_END_REPLACEMENT, "Drive"));

	private static final Cleaner ST = new Cleaner(String.format(REGEX_START_END, "ST"), String.format(REGEX_START_END_REPLACEMENT, "Street"));

	private static final Cleaner VI = new Cleaner(String.format(REGEX_START_END, "VI"), String.format(REGEX_START_END_REPLACEMENT, "Villas"));

	private static final Cleaner PZ = new Cleaner(String.format(REGEX_START_END, "PZ"), String.format(REGEX_START_END_REPLACEMENT, "Plaza"));

	private static final Cleaner WY = new Cleaner(String.format(REGEX_START_END, "WY"), String.format(REGEX_START_END_REPLACEMENT, "Way"));

	private static final Cleaner GR = new Cleaner(String.format(REGEX_START_END, "GR"), String.format(REGEX_START_END_REPLACEMENT, "Green"));

	private static final Cleaner BV = new Cleaner(String.format(REGEX_START_END, "BV"), String.format(REGEX_START_END_REPLACEMENT, "Boulevard"));

	private static final Cleaner GA = new Cleaner(String.format(REGEX_START_END, "GA"), String.format(REGEX_START_END_REPLACEMENT, "Gate"));

	private static final Cleaner RD = new Cleaner(String.format(REGEX_START_END, "RD"), String.format(REGEX_START_END_REPLACEMENT, "Road"));

	private static final Cleaner LI = new Cleaner(String.format(REGEX_START_END, "LI|LINK"), String.format(REGEX_START_END_REPLACEMENT, "Link"));

	private static final Cleaner PL = new Cleaner(String.format(REGEX_START_END, "PL"), String.format(REGEX_START_END_REPLACEMENT, "Place"));

	private static final Cleaner SQ = new Cleaner(String.format(REGEX_START_END, "SQ"), String.format(REGEX_START_END_REPLACEMENT, "Square"));

	private static final Cleaner CL = new Cleaner(String.format(REGEX_START_END, "CL"), String.format(REGEX_START_END_REPLACEMENT, "Close"));

	private static final Cleaner CR = new Cleaner(String.format(REGEX_START_END, "CR"), String.format(REGEX_START_END_REPLACEMENT, "Crescent"));

	private static final Cleaner GD = new Cleaner(String.format(REGEX_START_END, "GD"), String.format(REGEX_START_END_REPLACEMENT, "Gardens"));

	private static final Cleaner LN = new Cleaner(String.format(REGEX_START_END, "LN"), String.format(REGEX_START_END_REPLACEMENT, "Lane"));

	private static final Cleaner CO = new Cleaner(String.format(REGEX_START_END, "CO"), String.format(REGEX_START_END_REPLACEMENT, "Ct"));

	private static final Cleaner CI = new Cleaner(String.format(REGEX_START_END, "CI"), String.format(REGEX_START_END_REPLACEMENT, "Circle"));

	private static final Cleaner HE = new Cleaner(String.format(REGEX_START_END, "HE"), String.format(REGEX_START_END_REPLACEMENT, "Heath"));

	private static final Cleaner ME = new Cleaner(String.format(REGEX_START_END, "ME"), String.format(REGEX_START_END_REPLACEMENT, "Mews"));

	private static final Cleaner TR = new Cleaner(String.format(REGEX_START_END, "TR"), String.format(REGEX_START_END_REPLACEMENT, "Trail"));

	private static final Cleaner LD = new Cleaner(String.format(REGEX_START_END, "LD"), String.format(REGEX_START_END_REPLACEMENT, "Landing"));

	private static final Cleaner HL = new Cleaner(String.format(REGEX_START_END, "HL"), String.format(REGEX_START_END_REPLACEMENT, "Hill"));

	private static final Cleaner PK = new Cleaner(String.format(REGEX_START_END, "PK"), String.format(REGEX_START_END_REPLACEMENT, "Park"));

	private static final Cleaner CM = new Cleaner(String.format(REGEX_START_END, "CM"), String.format(REGEX_START_END_REPLACEMENT, "Common"));

	private static final Cleaner GT = new Cleaner(String.format(REGEX_START_END, "GT"), String.format(REGEX_START_END_REPLACEMENT, "Gate"));

	private static final Cleaner CV = new Cleaner(String.format(REGEX_START_END, "CV"), String.format(REGEX_START_END_REPLACEMENT, "Cove"));

	private static final Cleaner VW = new Cleaner(String.format(REGEX_START_END, "VW"), String.format(REGEX_START_END_REPLACEMENT, "View"));

	private static final Cleaner BY = new Cleaner(String.format(REGEX_START_END, "BY|BA|BAY"), String.format(REGEX_START_END_REPLACEMENT, "Bay"));

	private static final Cleaner CE = new Cleaner(String.format(REGEX_START_END, "CE"), String.format(REGEX_START_END_REPLACEMENT, "Center"));

	private static final Cleaner CTR = new Cleaner(String.format(REGEX_START_END, "CTR"), String.format(REGEX_START_END_REPLACEMENT, "Center"));

	private static final Cleaner PY_ = new Cleaner(String.format(REGEX_START_END, "PY"), String.format(REGEX_START_END_REPLACEMENT, "Parkway"));

	private static final Cleaner PR_ = new Cleaner(String.format(REGEX_START_END, "PR"), String.format(REGEX_START_END_REPLACEMENT, "Parade"));

	private static final Cleaner PS_ = new Cleaner(String.format(REGEX_START_END, "PS"), String.format(REGEX_START_END_REPLACEMENT, "Passage"));

	private static final Cleaner RO_ = new Cleaner(String.format(REGEX_START_END, "RO"), String.format(REGEX_START_END_REPLACEMENT, "Row"));

	private static final Cleaner MT_ = new Cleaner(String.format(REGEX_START_END, "MT"), String.format(REGEX_START_END_REPLACEMENT, "Mount"));

	private static final Cleaner GDN_ = new Cleaner(String.format(REGEX_START_END, "GDN"), String.format(REGEX_START_END_REPLACEMENT, "Garden"));

	private static final Cleaner TERR_ = new Cleaner(String.format(REGEX_START_END, "TERR"), String.format(REGEX_START_END_REPLACEMENT, "Terrace"));

	private static final Cleaner MOUNT_ROYAL_UNIVERSITY = new Cleaner(String.format(REGEX_START_END, "Mount Royal University"), String.format(REGEX_START_END_REPLACEMENT, "MRU"));

	private static final Cleaner MOUNT = new Cleaner(String.format(REGEX_START_END, "Mount"), String.format(REGEX_START_END_REPLACEMENT, "Mt"));

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = AV.clean(gStopName);
		gStopName = PA.clean(gStopName);
		gStopName = HT.clean(gStopName);
		gStopName = GV.clean(gStopName);
		gStopName = PT.clean(gStopName);
		gStopName = TC.clean(gStopName);
		gStopName = RI.clean(gStopName);
		gStopName = MR.clean(gStopName);
		gStopName = DR.clean(gStopName);
		gStopName = ST.clean(gStopName);
		gStopName = VI.clean(gStopName);
		gStopName = PZ.clean(gStopName);
		gStopName = WY.clean(gStopName);
		gStopName = GR.clean(gStopName);
		gStopName = BV.clean(gStopName);
		gStopName = GA.clean(gStopName);
		gStopName = RD.clean(gStopName);
		gStopName = LI.clean(gStopName);
		gStopName = PL.clean(gStopName);
		gStopName = SQ.clean(gStopName);
		gStopName = CL.clean(gStopName);
		gStopName = CR.clean(gStopName);
		gStopName = GD.clean(gStopName);
		gStopName = LN.clean(gStopName);
		gStopName = CO.clean(gStopName);
		gStopName = ME.clean(gStopName);
		gStopName = TR.clean(gStopName);
		gStopName = CI.clean(gStopName);
		gStopName = HE.clean(gStopName);
		gStopName = LD.clean(gStopName);
		gStopName = HL.clean(gStopName);
		gStopName = PK.clean(gStopName);
		gStopName = CM.clean(gStopName);
		gStopName = GT.clean(gStopName);
		gStopName = CV.clean(gStopName);
		gStopName = VW.clean(gStopName);
		gStopName = BY.clean(gStopName);
		gStopName = CE.clean(gStopName);
		gStopName = CTR.clean(gStopName);
		gStopName = PY_.clean(gStopName);
		gStopName = PR_.clean(gStopName);
		gStopName = PS_.clean(gStopName);
		gStopName = RO_.clean(gStopName);
		gStopName = MT_.clean(gStopName);
		gStopName = GDN_.clean(gStopName);
		gStopName = TERR_.clean(gStopName);
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CLEAN_AT_SPACE.clean(gStopName);
		gStopName = MOUNT_ROYAL_UNIVERSITY.clean(gStopName);
		gStopName = MOUNT.clean(gStopName);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = STARTS_WITH_SLASH.clean(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
