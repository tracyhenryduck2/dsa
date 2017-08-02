package com.google.zxing.client.android.result;

import android.app.Activity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ExpandedProductParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ProductParsedResult;
import com.henry.ecdemo.R;

public final class ProductResultHandler extends ResultHandler {
  private static final int[] buttons = {
      R.string.button_product_search,
      R.string.button_web_search,
      R.string.button_custom_product_search
  };

  public ProductResultHandler(Activity activity, ParsedResult result, Result rawResult) {
    super(activity, result, rawResult);
  }

  @Override
  public int getButtonCount() {
    return hasCustomProductSearch() ? buttons.length : buttons.length - 1;
  }

  @Override
  public int getButtonText(int index) {
    return buttons[index];
  }

  @Override
  public void handleButtonPress(int index) {
    String productID = getProductIDFromResult(getResult());
    switch (index) {
      case 0:
        openProductSearch(productID);
        break;
      case 1:
        webSearch(productID);
        break;
      case 2:
        openURL(fillInCustomSearchURL(productID));
        break;
    }
  }

  private static String getProductIDFromResult(ParsedResult rawResult) {
    if (rawResult instanceof ProductParsedResult) {
      return ((ProductParsedResult) rawResult).getNormalizedProductID();
    }
    if (rawResult instanceof ExpandedProductParsedResult) {
      return ((ExpandedProductParsedResult) rawResult).getRawText();
    }
    throw new IllegalArgumentException(rawResult.getClass().toString());
  }

  @Override
  public int getDisplayTitle() {
    return R.string.result_product;
  }
}
