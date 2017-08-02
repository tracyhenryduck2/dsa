
package com.google.zxing.client.android.result;

import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;


public final class ResultHandlerFactory {
  private ResultHandlerFactory() {
  }

  public static ResultHandler makeResultHandler(CaptureActivity activity, Result rawResult) {
    ParsedResult result = parseResult(rawResult);
    switch (result.getType()) {

      case PRODUCT:
        return new ProductResultHandler(activity, result, rawResult);
      case URI:
        return new URIResultHandler(activity, result);

      case GEO:
        return new GeoResultHandler(activity, result);
      case TEL:
        return new TelResultHandler(activity, result);

      case CALENDAR:
        return new CalendarResultHandler(activity, result);

      default:
        return new TextResultHandler(activity, result, rawResult);
    }
  }

  private static ParsedResult parseResult(Result rawResult) {
    return ResultParser.parseResult(rawResult);
  }
}
