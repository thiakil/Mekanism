package mekanism.client.render.obj.glowbj;

import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector2f;

/**
 * Created by Thiakil on 28/03/2019.
 */
public class Texture {
	public static Texture WHITE = new Texture("builtin/white", new Vector2f(0, 0), new Vector2f(1, 1), 0);
	protected String path;
	protected Vector2f position;
	protected Vector2f scale;
	protected float rotation;

	public Texture(String path) {
		this(path, new Vector2f(0, 0), new Vector2f(1, 1), 0);
	}

	public Texture(String path, Vector2f position, Vector2f scale, float rotation) {
		this.path = path;
		this.position = position;
		this.scale = scale;
		this.rotation = rotation;
	}

	public ResourceLocation getTextureLocation() {
		ResourceLocation loc = new ResourceLocation(this.path);
		return loc;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public void setPosition(Vector2f position) {
		this.position = position;
	}

	public Vector2f getPosition() {
		return this.position;
	}

	public void setScale(Vector2f scale) {
		this.scale = scale;
	}

	public Vector2f getScale() {
		return this.scale;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public float getRotation() {
		return this.rotation;
	}
}
