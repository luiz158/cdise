package com.ctp.javaone.archiver.plugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.environment.se.contexts.ThreadScoped;

import com.ctp.javaone.archiver.archive.ArchivingResult;
import com.ctp.javaone.archiver.archive.ArchivingTask;
import com.ctp.javaone.archiver.command.Command;
import com.ctp.javaone.archiver.persistence.Auditable;

@Command(value = "archive", async = true)
//@Command("archive")
@ThreadScoped
@Auditable
public class Archive implements Plugin {
    
    public static final int SIZE = 2;

    @Inject
    private Instance<ArchivingTask> archivingInstance;

    private ExecutorService executor;

    @Inject
    private ArchivingResult result;

    public Result executeCommand(String... params) {
        result.resetArchivedFilesCounter();
        if (params == null || params.length == 0) {
            throw new NullPointerException("Please pass pathname of the folder to be archived");
        }
        File source = new File(params[0]);
        if (!source.exists()) {
            throw new NullPointerException("The passed pathname does not exist.");
        }

        // TODO Introduce targetFolder as a new parameter
        File target = new File("." + File.separator + "target/" + params[0]);
        executor = Executors.newFixedThreadPool(SIZE);

        try {
            copyDirectory(source, target);
        } catch (IOException e) {
            throw new NullPointerException(e.getMessage());
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        int archivedFilesCount = result.getArchivedFilesCounter();
        
        return new Result("Archiving process concluded, total of archived files: " + archivedFilesCount, Status.SUCCESS);
    }

    private void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            ArchivingTask archiverTask = archivingInstance.get();
            archiverTask.setSource(sourceLocation);
            archiverTask.setTarget(targetLocation);
            executor.execute(archiverTask);
        }
    }
}
