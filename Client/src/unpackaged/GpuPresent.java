package unpackaged;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWGL;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.WGL;
import org.lwjgl.opengl.WGLARBCreateContext;
import org.lwjgl.opengl.WGLEXTSwapControl;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.windows.GDI32;
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR;
import org.lwjgl.system.windows.User32;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33C.*;

/**
 * GPU rendering straight to the window (resizable layout): instead of copying the world back to Java and
 * letting Java2D show it, an OpenGL context on the game canvas draws the world texture from GpuGL and lays
 * the HUD over it. The HUD is the frame Resizable composes as usual; where it still holds the WORLD marker
 * (nothing 2D was drawn there) the world shows through. Any failure turns this path off for good and the
 * copy-back path carries on.
 */
final class GpuPresent {
    /** Fills the view where the world is; 2D drawing always writes 0x00RRGGBB, so it never matches. */
    static final int WORLD = 0x01000000;

    private static long hwnd, hdc, context;
    private static GLCapabilities caps;
    private static int program, vao, hud, hudW, hudH, heightUniform;

    private static final String VERTEX =
        "#version 330 core\n"
        + "void main() {\n"
        + "  vec2 p = vec2((gl_VertexID & 1) * 4 - 1, (gl_VertexID >> 1) * 4 - 1);\n"   // one triangle over the screen
        + "  gl_Position = vec4(p, 0.0, 1.0);\n"
        + "}\n";

    private static final String FRAGMENT =
        "#version 330 core\n"
        + "uniform sampler2D uWorld;\n"
        + "uniform sampler2D uHud;\n"
        + "uniform int uHeight;\n"
        + "out vec4 fragColor;\n"
        + "void main() {\n"
        + "  ivec2 p = ivec2(int(gl_FragCoord.x), uHeight - 1 - int(gl_FragCoord.y));\n"   // rows run top down
        + "  vec4 hud = texelFetch(uHud, p, 0);\n"
        // the HUD is uploaded as BGRA, so its alpha is the int's top byte: 1/255 only for the WORLD marker
        + "  if (abs(hud.a - 1.0 / 255.0) < 0.5 / 255.0) fragColor = vec4(texelFetch(uWorld, p, 0).bgr, 1.0);\n"
        + "  else fragColor = vec4(hud.rgb, 1.0);\n"
        + "}\n";

    private GpuPresent() { }

    /** Shows the world plus this HUD frame on the canvas. False: not possible, use the Java2D path instead. */
    static boolean present(java.awt.Component canvas, int[] frame, int w, int h) {
        if (Gpu.presentFailed) return false;
        try {
            if (!bind(canvas)) return false;
            glViewport(0, 0, w, h);
            if (GpuGL.worldDone != 0) {
                glWaitSync(GpuGL.worldDone, 0, GL_TIMEOUT_IGNORED);
                glDeleteSync(GpuGL.worldDone);
                GpuGL.worldDone = 0;
            }
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, hud);
            if (hudW != w || hudH != h) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
                hudW = w;
                hudH = h;
            }
            glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, w, h, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, frame);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, GpuGL.worldTexture());
            glUseProgram(program);
            glUniform1i(heightUniform, h);
            glBindVertexArray(vao);
            glDisable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            GDI32.SwapBuffers(hdc);
            Gpu.presentedAt = System.currentTimeMillis();
            int err = glGetError();
            if (err != GL_NO_ERROR) throw new IllegalStateException("OpenGL error 0x" + Integer.toHexString(err));
            return true;
        } catch (Throwable t) {
            Gpu.presentFailed = true;
            System.out.println("[GPU] drawing straight to the window turned off: " + t);
            t.printStackTrace(System.out);
            return false;
        } finally {
            unbind();
        }
    }

    /** Make the canvas context current (creating it, sharing GpuGL's textures, the first time). */
    private static boolean bind(java.awt.Component canvas) throws Exception {
        long h = hwndOf(canvas);
        if (h == 0) return false;
        if (h != hwnd) create(h);
        if (!WGL.wglMakeCurrent(hdc, context)) throw new IllegalStateException("could not use the window's OpenGL context");
        GL.setCapabilities(caps);
        return true;
    }

    /** Back to GpuGL's own context, which the world drawing expects to be current. */
    private static void unbind() {
        long w = GpuGL.window();
        if (w == 0) return;
        GLFW.glfwMakeContextCurrent(w);
        GL.setCapabilities(GpuGL.caps());
    }

    private static void create(long h) {
        if (hdc != 0) User32.ReleaseDC(hwnd, hdc);
        hwnd = h;
        hdc = User32.GetDC(h);
        if (hdc == 0) throw new IllegalStateException("no device context for the game window");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (GDI32.GetPixelFormat(hdc) == 0) {
                PIXELFORMATDESCRIPTOR pfd = PIXELFORMATDESCRIPTOR.calloc(stack)
                    .nSize((short) PIXELFORMATDESCRIPTOR.SIZEOF)
                    .nVersion((short) 1)
                    .dwFlags(GDI32.PFD_DRAW_TO_WINDOW | GDI32.PFD_SUPPORT_OPENGL | GDI32.PFD_DOUBLEBUFFER)
                    .iPixelType(GDI32.PFD_TYPE_RGBA)
                    .cColorBits((byte) 32)
                    .cAlphaBits((byte) 8)
                    .iLayerType(GDI32.PFD_MAIN_PLANE);
                int format = GDI32.ChoosePixelFormat(hdc, pfd);
                if (format == 0 || !GDI32.SetPixelFormat(hdc, format, pfd))
                    throw new IllegalStateException("the game window refused an OpenGL pixel format");
            }
            // created while GpuGL's context is current, sharing its objects (the world texture)
            long share = GLFWNativeWGL.glfwGetWGLContext(GpuGL.window());
            IntBuffer attribs = stack.ints(
                WGLARBCreateContext.WGL_CONTEXT_MAJOR_VERSION_ARB, 3,
                WGLARBCreateContext.WGL_CONTEXT_MINOR_VERSION_ARB, 3,
                0x9126 /* WGL_CONTEXT_PROFILE_MASK_ARB */, 0x1 /* core */,
                0);
            context = WGLARBCreateContext.wglCreateContextAttribsARB(hdc, share, attribs);
        }
        if (context == 0) throw new IllegalStateException("could not create an OpenGL context on the game window");
        if (!WGL.wglMakeCurrent(hdc, context)) throw new IllegalStateException("could not use the window's OpenGL context");
        caps = GL.createCapabilities();
        if (GL.getCapabilitiesWGL().WGL_EXT_swap_control) WGLEXTSwapControl.wglSwapIntervalEXT(0);   // FramePacer sets the pace
        program = glCreateProgram();
        int vs = shader(GL_VERTEX_SHADER, VERTEX), fs = shader(GL_FRAGMENT_SHADER, FRAGMENT);
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) throw new IllegalStateException("present shader: " + glGetProgramInfoLog(program));
        glUseProgram(program);
        glUniform1i(glGetUniformLocation(program, "uWorld"), 0);
        glUniform1i(glGetUniformLocation(program, "uHud"), 1);
        heightUniform = glGetUniformLocation(program, "uHeight");
        vao = glGenVertexArrays();
        hud = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hud);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        hudW = hudH = 0;
        System.out.println("[GPU] drawing straight to the game window");
    }

    private static int shader(int type, String source) {
        int s = glCreateShader(type);
        glShaderSource(s, source);
        glCompileShader(s);
        if (glGetShaderi(s, GL_COMPILE_STATUS) == GL_FALSE) throw new IllegalStateException("present shader: " + glGetShaderInfoLog(s));
        return s;
    }

    /** The canvas's native window, through its AWT peer (Java 8 on Windows). */
    private static long hwndOf(java.awt.Component c) throws Exception {
        @SuppressWarnings("deprecation")
        Object peer = c.getPeer();
        if (peer == null) return 0;
        java.lang.reflect.Method m = peer.getClass().getMethod("getHWnd");
        m.setAccessible(true);
        return (Long) m.invoke(peer);
    }

    /** GpuGL's context went away: this one shared its objects, so start over next time. */
    static void dispose() {
        if (context != 0) {
            WGL.wglMakeCurrent(0, 0);
            WGL.wglDeleteContext(context);
        }
        if (hdc != 0) User32.ReleaseDC(hwnd, hdc);
        context = hdc = hwnd = 0;
        caps = null;
        unbind();                                          // GpuGL's context current again for the next world
    }
}
