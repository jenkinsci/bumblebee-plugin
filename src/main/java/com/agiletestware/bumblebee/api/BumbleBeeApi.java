package com.agiletestware.bumblebee.api;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import com.agiletestware.bumblebee.BumblebeeRemoteExecutor.Parameters;
import com.agiletestware.bumblebee.util.BumblebeeUtils;
import com.agiletestware.bumblebee.util.StringBuilderWrapper;

/**
 * Class to communicate with BumbleBee Webservice API.
 *
 * @author Sergey Oplavin (oplavin.sergei@gmail.com) Refactored the code.
 *
 */
public class BumbleBeeApi {

	private static final String DATE_FORMAT_PATTERN = "MM/dd/yyyy";
	private static final String EXPIRE_DATE = "ExpireDate";
	private static final String CREATE_DATE = "CreateDate";
	private final String bumblebeeUrl;
	private final int timeOut;

	/**
	 * Creates instance.
	 *
	 * @param bumbleBeeUrl
	 *            The URL of BumbleBee instance.
	 * @param timeOutInSeconds
	 *            Time out for HTTP Connections in seconds. 0 - infinite
	 *            timeout. Less than 0 - use operating system default.
	 */
	public BumbleBeeApi(final String bumbleBeeUrl, final int timeOutInSeconds) {
		this.bumblebeeUrl = bumbleBeeUrl;
		this.timeOut = timeOutInSeconds * 1000;
	}

	/**
	 * Validates the bumblebee server license. Throws {@link BumbleBeeException}
	 * if license is expired or invalid.
	 *
	 * @throws IOException
	 * @throws BumbleBeeException
	 * @throws JSONException
	 */
	public void validateLicense() throws IOException, BumbleBeeException,
			JSONException {
		final String uri = "/licenseinfo";
		final JSONObject responseBody = executeSimpleGetRequest(uri);
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				DATE_FORMAT_PATTERN);
		final String createDateStr = responseBody.optString(CREATE_DATE);
		final String expireDateStr = responseBody.optString(EXPIRE_DATE);

		if (StringUtils.isBlank(createDateStr)
				|| StringUtils.isBlank(expireDateStr)) {
			throw new BumbleBeeException(
					"Cannot retrieve CreateDate or ExpireDate from bumblebee server. Please contact Agiletestware support at contact@agiletestware.com");
		}
		try {
			final Date createDate = dateFormat.parse(createDateStr);
			final Date expireDate = dateFormat.parse(expireDateStr);
			final Calendar calendar = Calendar.getInstance();
			// do not count time differences
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			final Date currentDate = calendar.getTime();
			if ( // if license create date is a future date
			(currentDate.compareTo(createDate) < 0) ||
			// OR if expire date has passed
					(currentDate.compareTo(expireDate) > 0)) {
				throw new BumbleBeeException(
						"Invalid or expired license. Please contact Agiletestware support at contact@agiletestware.com");
			}

		} catch (final ParseException ex) {
			throw new BumbleBeeException(
					MessageFormat
							.format("Bumblebee returned either {0}:''{1}'' or {2}:''{3}'' has invalid format: {4}",
									CREATE_DATE, createDateStr, EXPIRE_DATE,
									expireDateStr, DATE_FORMAT_PATTERN));
		}
	}

	/**
	 * Get the license expiration date.
	 *
	 * @return the license expiration date.
	 * @throws IOException
	 * @throws BumbleBeeException
	 * @throws JSONException
	 */
	public String getLicenseExpirationDate() throws IOException,
			BumbleBeeException, JSONException {
		final String uri = "/licenseinfo";
		final JSONObject responseBody = executeSimpleGetRequest(uri);
		return responseBody.optString(EXPIRE_DATE);
	}

	/**
	 * Encrypts the password string on bumblebee server.
	 *
	 * @param password
	 *            The password string to encrypt.
	 * @return Encrypted value.
	 * @throws IOException
	 * @throws BumbleBeeException
	 * @throws JSONException
	 */
	public String getEncryptedPassword(final String password)
			throws IOException, BumbleBeeException, JSONException {
		final String uri = "/password?password=" + password;
		final JSONObject responseBody = executeSimpleGetRequest(uri);
		return responseBody.optString("message");
	}

	/**
	 * Executes simple HTTP GET request to given uri.
	 *
	 * @param uri
	 *            The uri on the bumblebee server. E.g. /licenseInfo.
	 * @return {@link JSONObject} - JSON representation of response content if
	 *         any.
	 * @throws IOException
	 * @throws BumbleBeeException
	 * @throws JSONException
	 */
	private JSONObject executeSimpleGetRequest(final String uri)
			throws IOException, BumbleBeeException, JSONException {
		final String url = bumblebeeUrl + uri;
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(timeOut).setConnectTimeout(timeOut)
					.build();
			final HttpGet httpGet = new HttpGet(url);
			httpGet.setConfig(requestConfig);
			httpGet.addHeader("accept", "application/json");
			httpGet.addHeader("Content-type", "application/json; charset=UTF-8");
			try (CloseableHttpResponse response = client.execute(httpGet)) {
				final StatusLine statusLine = response.getStatusLine();
				final int statusCode = statusLine.getStatusCode();
				final String errorReason = statusLine.getReasonPhrase();
				if (statusCode == 255) {
					throw new BumbleBeeException(
							String.format(
									"Invalid or expired license. Please contact Agiletestware support at contact@agiletestware.com :%d %s",
									statusCode, errorReason));
				}
				if ((statusCode >= 300) || (statusCode < 200)) {
					throw new BumbleBeeException(String.format("Failed: %d %s",
							statusCode, errorReason));
				}
				final HttpEntity entity = response.getEntity();
				if (entity == null || entity.getContentLength() == 0) {
					throw new BumbleBeeException(
							"HTTP Response does not have content");
				}
				final String entityContent = EntityUtils.toString(entity);
				return new JSONObject(entityContent);
			}

		}

	}

	/**
	 * Upload single file to bumblebee.
	 *
	 * @param filePath
	 *            The file path
	 * @param parameters
	 *            parameters
	 * @param log
	 *            console to log into
	 * @return <code>true</code> if file has been uploaded successfully,
	 *         <code>false</code> otherwise.
	 * @throws IOException
	 * @throws BumbleBeeException
	 * @throws JSONException
	 */
	public boolean uploadSingleFile(final FilePath filePath,
			final Parameters parameters, final StringBuilderWrapper log)
			throws IOException, BumbleBeeException, JSONException {
		boolean fileUploaded = false;
		log.println("Uploading file :" + filePath.getRemote() + "\n");
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final HttpPost post = createPostRequest(filePath, parameters);
			log.println(BumblebeeUtils
					.maskPasswordInString("Request POST URL : "
							+ post.getRequestLine()));
			try (CloseableHttpResponse response = httpclient.execute(post)) {
				final StatusLine statusLine = response.getStatusLine();
				final int statusCode = statusLine.getStatusCode();
				if (statusCode == 255) {
					throw new BumbleBeeException(
							"The bumblebee license expired on "
									+ getLicenseExpirationDate()
									+ ". Please email contact@agiletestware.com for license renewal");
				}
				final HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					final String page = StringEscapeUtils
							.unescapeXml(EntityUtils.toString(resEntity));
					fileUploaded = page
							.contains("<message>FULL BULK UPDATE SUCCESS</message>");
					log.println("Response :"
							+ extractMessageStringFromResponseForLogging(page));
				}
			}
		}
		return fileUploaded;
	}

	/**
	 * Create HTTP post request.
	 *
	 * @param filePath
	 *            The file path to upload.
	 * @param parameters
	 *            parameters
	 * @return prepared http post request.
	 * @throws UnsupportedEncodingException
	 */
	private HttpPost createPostRequest(final FilePath filePath,
			final Parameters parameters) throws UnsupportedEncodingException {
		final String bulkUpdateUrl = getUrlForQcUpdate(parameters);
		final HttpPost post = new HttpPost(bulkUpdateUrl);
		final RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(timeOut).setConnectTimeout(timeOut).build();
		post.setConfig(requestConfig);
		final MultipartEntityBuilder reqEntity = MultipartEntityBuilder
				.create();
		reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addTextBody("url", parameters.getQcUrl());
		reqEntity.addTextBody("qcUserName", parameters.getQcUserName());
		reqEntity.addTextBody("format", parameters.getFormat());
		reqEntity.addTextBody("domain", parameters.getDomain());
		reqEntity.addTextBody("project", parameters.getProject());
		reqEntity.addTextBody("testplandirectory",
				parameters.getTestplandirectory());
		reqEntity.addTextBody("testlabdirectory",
				parameters.getTestlabdirectory());
		reqEntity.addTextBody("testset", parameters.getTestSet());
		reqEntity.addTextBody("mode", parameters.getMode());
		reqEntity.addTextBody("encrypted_password",
				parameters.getEncryptedPassword());

		final FileBody bin = new FileBody(new File(filePath.getRemote()));
		reqEntity.addPart("attachment", bin);
		post.setEntity(reqEntity.build());
		return post;
	}

	/**
	 * Extracts message from response string. If there is no message element in
	 * the given string then the whole response string is returned. Password is
	 * masked.
	 *
	 * @param response
	 *            the response
	 * @return message from response string. If there is no message element in
	 *         the given string then the whole response string is returned.
	 *         Password is masked.
	 */
	private String extractMessageStringFromResponseForLogging(
			final String response) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		factory.setValidating(false);
		try (InputStream stream = new StringInputStream(response)) {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final XPath xPath = XPathFactory.newInstance().newXPath();
			final String message = xPath.compile("//message").evaluate(builder);
			if (StringUtils.isNotBlank(message)) {
				return message;
			}
		} catch (final Exception ex) {
			// do nothing
		}
		return BumblebeeUtils.maskPasswordInResponse(response);
	}

	/**
	 * Create URL for uploading files to bumblebee.
	 *
	 * @param parameters
	 *            the parameters
	 * @return created URL.
	 * @throws UnsupportedEncodingException
	 */
	public String getUrlForQcUpdate(final Parameters parameters)
			throws UnsupportedEncodingException {
		final String utf8Encoding = StandardCharsets.UTF_8.name();
		final String encryptedPass = URLEncoder.encode(
				parameters.getEncryptedPassword(), utf8Encoding);
		final String qcUrl = URLEncoder.encode(parameters.getQcUrl(),
				utf8Encoding);
		final String qcUserName = URLEncoder.encode(parameters.getQcUserName(),
				utf8Encoding);
		final String domain = URLEncoder.encode(parameters.getDomain(),
				utf8Encoding);
		final String project = URLEncoder.encode(parameters.getProject(),
				utf8Encoding);
		final String testPlanDirectory = URLEncoder.encode(
				parameters.getTestplandirectory(), utf8Encoding);
		final String testLabDirectory = URLEncoder.encode(
				parameters.getTestlabdirectory(), utf8Encoding);
		final String testSet = URLEncoder.encode(parameters.getTestSet(),
				utf8Encoding);
		final String format = URLEncoder.encode(parameters.getFormat(),
				utf8Encoding);
		String customProperties = parameters.getCustomProperties();
		if (customProperties != null) {
			customProperties = URLEncoder
					.encode(customProperties, utf8Encoding);
			customProperties = customProperties.replaceAll("%3D", "=")
					.replaceAll("%2C", "&");
		}
		if (customProperties != null) {
			return String
					.format("%s/updateqcbulk?url=%s&user=%s&encrypted_password=%s&domain=%s&project=%s&testplandirectory=%s&testlabdirectory=%s&testset=%s&format=%s&mode=FULL&%s",
							bumblebeeUrl, qcUrl, qcUserName, encryptedPass,
							domain, project, testPlanDirectory,
							testLabDirectory, testSet, format, customProperties);
		} else {
			return String
					.format("%s/updateqcbulk?url=%s&user=%s&encrypted_password=%s&domain=%s&project=%s&testplandirectory=%s&testlabdirectory=%s&testset=%s&format=%s&mode=FULL",
							bumblebeeUrl, qcUrl, qcUserName, encryptedPass,
							domain, project, testPlanDirectory,
							testLabDirectory, testSet, format);
		}

	}

}
