package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sandros Print Labor", appName)
  }

  @Test
  fun `test slicing and save project`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.ProjectRepository(context)
    val mesh = com.example.engine.SampleModels.createCalibrationCube()
    val config = com.example.model.SlicingConfig()
    val slicer = com.example.service.slicer.CrealityCR10SProSlicer()
    var completedResult: com.example.model.GcodeResult? = null
    slicer.slice(mesh, config).collect { progress ->
      if (progress is com.example.service.slicer.SlicingProgress.Completed) {
        completedResult = progress.result
      }
    }
    org.junit.Assert.assertNotNull(completedResult)
    val p = com.example.model.Project(
      id = "test_1",
      title = "Test",
      status = com.example.model.ProjectStatus.SLICED,
      mesh = mesh,
      slicingConfig = config,
      gcodeResult = completedResult
    )
    repo.saveProject(p)
    val loaded = repo.getProject("test_1")
    org.junit.Assert.assertNotNull(loaded)
    org.junit.Assert.assertEquals(completedResult?.id, loaded?.gcodeResult?.id)
  }
}

